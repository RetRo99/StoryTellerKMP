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

    // Configure KSP-generated code to be available to all source sets
    pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
        pluginManager.withPlugin("com.google.devtools.ksp") {
            afterEvaluate {
                // Make all Kotlin compilation tasks depend on kspCommonMainKotlinMetadata
                tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask<*>>()
                    .configureEach {
                        if (name != "kspCommonMainKotlinMetadata") {
                            dependsOn("kspCommonMainKotlinMetadata")
                        }
                    }

                // Make all KSP tasks depend on kspCommonMainKotlinMetadata
                tasks.matching { it.name.startsWith("ksp") && it.name != "kspCommonMainKotlinMetadata" }
                    .configureEach {
                        dependsOn("kspCommonMainKotlinMetadata")
                    }

                extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension> {
                    sourceSets.getByName("commonMain") {
                        kotlin.srcDir("${layout.buildDirectory.get()}/generated/ksp/metadata/commonMain/kotlin")
                    }
                }
            }
        }
    }
}