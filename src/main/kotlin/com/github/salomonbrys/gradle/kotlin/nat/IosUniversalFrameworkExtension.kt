package com.github.salomonbrys.gradle.kotlin.nat

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.kotlin.dsl.container
import org.gradle.kotlin.dsl.getByType
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

class IosUniversalFrameworkExtension(project: Project) {

    private val kotlin = project.extensions.getByType<KotlinMultiplatformExtension>()

    val frameworks = project.container { UniversalFramework(it, project, kotlin) }

    @JvmOverloads
    fun ios(action: Action<UniversalFramework> = Action {}) {
        val framework = frameworks.maybeCreate("ios")
        framework.from("iosArm32", "iosArm64", "iosX64")
        action.execute(framework)
    }

    fun framework(name: String, action: Action<UniversalFramework>) {
        val framework = frameworks.maybeCreate(name)
        action.execute(framework)
    }

}
