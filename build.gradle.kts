plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.kotzilla) apply false
}

subprojects {
    pluginManager.withPlugin("com.google.devtools.ksp") {
        extensions.configure<com.google.devtools.ksp.gradle.KspExtension> {
            arg("KOIN_CONFIG_CHECK", "true")
        }
    }
}