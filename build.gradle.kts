
plugins {
    kotlin("jvm") version "1.3.40"
    `maven-publish`
    id("com.gradle.plugin-publish") version "0.9.10"
}

group = "com.github.salomonbrys.gradle.kotlin.nat.ios"
version = "1.0.0"

repositories {
    jcenter()
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("gradle-plugin"))
    implementation(gradleApi())
    implementation(gradleKotlinDsl())
}

kotlin {
    target {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])
        }
    }
}

pluginBundle {
    website = "https://github.com/SalomonBrys/kotlin-ios-universal-framework"
    vcsUrl = "https://github.com/SalomonBrys/kotlin-ios-universal-framework.git"
    tags = listOf("kotlin", "kotlin-native", "kotlin-multiplatform", "ios")

    plugins {
        create(project.name) {
            id = "com.github.salomonbrys.gradle.kotlin.nat.ios-universal-framework"
            description = "A Gradle plugin that generates a universal framework from Kotlin/Native frameworks and a binary associated podspec."
            displayName = project.name
        }
    }
}
