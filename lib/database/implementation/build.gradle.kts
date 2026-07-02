

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidKotlinMultiplatformLibrary)
    alias(libs.plugins.ksp)
    alias(libs.plugins.koinCompilerPlugin)
    alias(libs.plugins.sqldelight)
}

version = "1.0"

kotlin {
    jvmToolchain(libs.versions.jdk.get().toInt())

    androidLibrary {
        namespace = "com.retro99.database.implementation"
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
            implementation(libs.koin.core)
            api(libs.koin.annotations)
            implementation(libs.datetime)
            implementation(libs.coroutines)
            implementation(projects.lib.database.api)
            implementation(projects.lib.preferences.api)
            implementation(projects.lib.user.api)
            implementation(projects.base)
            implementation(projects.lib.analytics.api)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
        }

        androidMain.dependencies {
            implementation(libs.sqldelight.android.driver)
        }

        iosMain.dependencies {
            implementation(libs.sqldelight.native.driver)
        }

        jsMain.dependencies {
            implementation(libs.sqldelight.web.worker.driver)
            implementation(devNpm("copy-webpack-plugin", "9.1.0"))
            implementation(npm("@cashapp/sqldelight-sqljs-worker", "2.3.2"))
            implementation(npm("sql.js", "1.8.0"))
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.retro99.database.implementation")
            version = 13
            generateAsync.set(true)
        }
    }
}
