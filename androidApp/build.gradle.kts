plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

android {
    namespace = "org.retro99.storyteller"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "org.retro99.storyteller"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.toVersion(libs.versions.jdk.get().toInt())
        targetCompatibility = JavaVersion.toVersion(libs.versions.jdk.get().toInt())
    }
}

dependencies {
    implementation(projects.composeApp)
    implementation(libs.compose.uiToolingPreview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.koin.android)
    debugImplementation(libs.compose.uiTooling)
}

