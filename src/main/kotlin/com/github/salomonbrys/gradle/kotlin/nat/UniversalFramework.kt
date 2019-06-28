package com.github.salomonbrys.gradle.kotlin.nat

import org.gradle.api.Action
import org.gradle.api.Named
import org.gradle.api.Project
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

class UniversalFramework(private val name: String, private val project: Project, private val kotlin: KotlinMultiplatformExtension) : Named {

    val targets = ArrayList<KotlinNativeTarget>()

    private var _podspec: Podspec? = null

    internal fun hasPodspec() = _podspec != null

    val podspec: Podspec get() = _podspec ?: Podspec(project, this).also { _podspec = it }

    fun podspec(action: Action<Podspec>) = action.execute(podspec)

    override fun getName() = name

    fun from(vararg targets: String) = this.targets.addAll(targets.mapNotNull {
        val target = kotlin.targets.findByName(it) as? KotlinNativeTarget
        if (target == null)
            project.logger.warn("Unknown target $it will not be included in universal framework $name")
        target
    })

    fun from(vararg targets: KotlinTarget) = this.targets.addAll(targets.map { it as KotlinNativeTarget })

}
