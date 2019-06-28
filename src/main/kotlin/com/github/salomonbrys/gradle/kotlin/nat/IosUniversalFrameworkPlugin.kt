package com.github.salomonbrys.gradle.kotlin.nat

import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.bundling.Zip
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.task
import org.jetbrains.kotlin.gradle.plugin.mpp.Framework
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeOutputKind

class IosUniversalFrameworkPlugin : Plugin<Project> {

    override fun apply(project: Project) = project.applyPlugin()

    private fun Project.applyPlugin() {
        val conf = IosUniversalFrameworkExtension(this)
        extensions.add("universalFrameworks", conf)

        afterEvaluate {
            conf.frameworks.forEach { framework ->
                if (framework.targets.isEmpty()) {
                    logger.warn("Universal framework ${framework.name} has no configured target")
                    return@forEach
                }

                framework.targets.forEach {
                    if (it.binaries.none { it.outputKind == NativeOutputKind.FRAMEWORK })
                        throw InvalidUserDataException("Target ${it.name} does not build any framework.")
                }

                val buildTypes = framework.targets
                        .flatMap { it.binaries.map { it.buildType } }
                        .toSet()

                for (buildType in buildTypes) {
                    val buildTypeName = buildType.name.toLowerCase()

                    val binaries = framework.targets.flatMap {
                        it.binaries
                                .filterIsInstance<Framework>()
                                .filter { it.buildType == buildType }
                    }

                    val lipoTask = task<Copy>("${framework.name}LipoUniversal${buildTypeName.capitalize()}") {
                        val outputFramework = buildDir.resolve("bin/${framework.name}Universal/${buildTypeName}Framework/${project.name}.framework")
                        group = "build"
                        dependsOn(binaries.map { it.linkTask })
                        from(binaries.first().outputFile)
                        into(outputFramework)
                        exclude(project.name)

                        doLast {
                            exec {
                                it.executable = "lipo"
                                it.args = listOf("-create", "-output", outputFramework.resolve(project.name).absolutePath) +
                                        binaries.map { it.outputFile.resolve(project.name).absolutePath }
                            }
                        }
                    }

                    val zipTask = task<Zip>("${framework.name}ZipUniversal${buildTypeName.capitalize()}") {
                        group = "build"
                        dependsOn(lipoTask)
                        from(lipoTask.outputs.files)
                        into(lipoTask.outputs.files.first().name)
                        archiveName = "${project.name}-${framework.name}-${project.version}-$buildTypeName.zip"
                    }

                    if (framework.hasPodspec()) {
                        val genPodspecTask = task<Task>("${framework.name}Gen${buildTypeName.capitalize()}Podspec") {
                            group = "build"
                            val output = buildDir.resolve("publications/${framework.name}-framework/$buildTypeName.podspec")
                            outputs.files(output)

                            doLast {
                                mkdir(output.parentFile)
                                file(output).writeText(framework.podspec.toString(buildType))
                            }
                        }

                        project.pluginManager.withPlugin("maven-publish") {
                            project.extensions.configure<PublishingExtension> {
                                publications {
                                    it.maybeCreate("${framework.name}-framework", MavenPublication::class.java).apply {
                                        artifact(zipTask) {
                                            it.classifier = buildTypeName
                                        }
                                        artifact(genPodspecTask.outputs.files.first()) {
                                            it.classifier = buildTypeName
                                            it.builtBy(genPodspecTask)
                                        }
                                        artifactId = "${project.name}-${framework.name}-framework"
                                    }
                                }
                            }
                        }
                    }

                }
            }

        }
    }

}
