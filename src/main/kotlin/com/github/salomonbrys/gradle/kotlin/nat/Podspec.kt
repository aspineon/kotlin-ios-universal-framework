package com.github.salomonbrys.gradle.kotlin.nat

import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.plugin.mpp.NativeBuildType

class Podspec(private val project: Project, private val framework: UniversalFramework) {

    var summary: String = project.name
    var homePage: String = ""
    var license: String = "unknown"
    var iosDeploymentTarget: String = "8.0"
    var socialMediaUrl: String? = null
    var description: String? = null
    var documentationUrl: String? = null

    val authors = LinkedHashMap<String, String>()
    fun authors(map: Map<String, String>) = authors.putAll(map)
    fun authors(vararg pairs: Pair<String, String>) = authors.putAll(pairs)

    var packageUrl: (String) -> String = { "" }
    fun url(base: String): (String) -> String = { "$base/${project.name}-${framework.name}-framework-${project.version}-$it.zip" }
    fun maven(base: String) = url("$base/${project.group.toString().replace('.', '/')}/${project.name}-${framework.name}-framework/${project.version}")

    val screenshots = ArrayList<String>()
    fun screenshots(vararg urls: String) = screenshots.addAll(urls)

    private fun String.escapeQuotes() = replace("'", "\\'")

    internal fun toString(buildType: NativeBuildType) = buildString {
        appendln("""
            Pod::Spec.new do |spec|
                spec.name                     = '${project.name.escapeQuotes()}-${buildType.name.toLowerCase()}'
                spec.version                  = '${project.version.toString().escapeQuotes()}'
                spec.summary                  = '${summary.escapeQuotes()}'
                spec.homepage                 = '${homePage.escapeQuotes()}'
                spec.license                  = '${license.escapeQuotes()}'
                spec.authors                  = { ${authors.entries.joinToString { "'${it.key.escapeQuotes()}' => '${it.value.escapeQuotes()}'" }} }
                spec.source                   = { :http => '${packageUrl(buildType.name.toLowerCase()).escapeQuotes()}' }
                spec.vendored_frameworks      = '${project.name.escapeQuotes()}.framework'
                spec.platform                 = :ios
                spec.ios.deployment_target    = '${iosDeploymentTarget.escapeQuotes()}'
        """.trimIndent())
        socialMediaUrl?.let   { appendln("    spec.social_media_url         = '${it.escapeQuotes()}'") }
        description?.let      { appendln("    spec.description              = '${it.escapeQuotes()}'") }
        documentationUrl?.let { appendln("    spec.documentation_url        = '${it.escapeQuotes()}'") }
        when {
            screenshots.size == 1 ->appendln("    spec.screenshot               = '${screenshots.first().escapeQuotes()}'")
            screenshots.size >= 2 ->appendln("    spec.screenshots              = [ ${screenshots.joinToString { "'${it.escapeQuotes()}'" }} ]")
        }
        appendln("end")
    }
}
