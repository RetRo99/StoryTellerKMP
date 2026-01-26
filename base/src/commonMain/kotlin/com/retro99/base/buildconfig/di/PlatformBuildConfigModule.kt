package com.retro99.base.buildconfig.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module that provides the BuildConfig implementation.
 *
 * This uses traditional Koin DSL instead of annotations because KSP doesn't
 * process platform-specific source sets (androidMain, iosMain) for annotations.
 */
expect val platformBuildConfigModule: Module

