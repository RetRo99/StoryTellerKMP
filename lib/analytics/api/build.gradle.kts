

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
}

version = "1.0"

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())

    androidLibrary {
        namespace = "com.retro99.analytics.api"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    iosArm64()
    iosSimulatorArm64()

    js {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
        }
    }
}
