Kotlin iOS Universal Framework
==============================

A Gradle plugin that generates a universal framework from Kotlin/Native frameworks and a binary associated podspec.

Usage
-----

### Universal Framework

Simply apply the plugin :

```kotlin
plugins {
    kotlin("multiplatform") version "1.3.40"
    id("com.github.salomonbrys.gradle.kotlin.nat.ios-universal-framework") version "1.0.0"
}
```

Then, after having configured Kotlin/Native ios targets, declare a universal framework:

```kotlin
kotlin {
    iosArm32()
    iosArm64()
    iosX64()
}

universalFrameworks {
    ios() // Creates a universal framework named "ios" for targets "iosArm32", "iosArm64" & "iosX64"
}
```

If you are naming your targets, you need to provide their names to the universal framework:

```kotlin
kotlin {
    iosArm32("iphone32")
    iosArm64("iphone64")
    iosX64("iphoneSimulator")
}

universalFrameworks {
    framework("iphone") {
        from("iphone32", "iphone64", "iphoneSimulator")
    }
}
```

That's it! The plugin creates the `iosLipoUniversalDebug` & `iosLipoUniversalRelease` tasks that create the universal frameworks.


### Binary podspec

The plugin can generate a podspec & a corresponding maven publication to allow you to distribute your package in a binary form using CocoaPods.

You need to define some properties for the podspec to be properly generated:

```kotlin
universalFrameworks {
    ios {
        podspec {
            // Required
            packageUrl = maven("https://dl.bintray.com/you/repo")
                   // or url("https://whatever.com/path/to/folder")
        
            // Optional
            homePage = "https://github.com/yourpage"
            license = "license-name"
            authors("author1" to "author1@email.com", "author2" to "author2@email.com")
            iosDeploymentTarget = "9.0" // default to "8.0"
            socialMediaUrl = "https://twitter.com/MeMyselfAndI"
            description = "Swag, swag, swaaag!"
            documentationUrl = "https://mylib.github.io/"
            screenshots("https://mylib.github.io/screen1.png", "https://mylib.github.io/screen2.png")
        }
    }
}
```

The plugin then creates:

- The `iosGenDebugPodspec` & `iosGenReleasePodspec` tasks that generate the podspec files.
- The "ios-framework" publication for maven deployment.
