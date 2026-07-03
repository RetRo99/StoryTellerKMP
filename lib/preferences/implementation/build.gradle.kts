plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.koinCompilerPlugin)
    alias(libs.plugins.kotlinxSerialization)
}

version = "1.0"

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())

    androidLibrary {
        namespace = "com.retro99.preferences.implementation"
        compileSdk = libs.versions.compileSdk.get().toInt()
        minSdk = libs.versions.minSdk.get().toInt()
    }

    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(libs.koin.core)
            api(libs.koin.annotations)
            api(libs.multiplatformSettings)
            implementation(libs.multiplatformSettingsCoroutines)
            implementation(libs.coroutines)
            implementation(libs.serialization)
            implementation(projects.base)
            implementation(projects.lib.preferences.api)
            implementation(projects.lib.user.api)
        }

        androidMain.dependencies {
            implementation(libs.androidx.security.crypto)
        }

        iosMain.dependencies {
        }
    }
}
