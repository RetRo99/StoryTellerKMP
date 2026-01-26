package com.retro99.preferences.implementation.di

import org.koin.core.module.Module

/**
 * Platform-specific Koin module that provides the Settings.Factory implementation.
 *
 * This uses traditional Koin DSL instead of annotations because KSP doesn't
 * process platform-specific source sets (androidMain, iosMain) for annotations.
 */
expect val platformPreferencesModule: Module

