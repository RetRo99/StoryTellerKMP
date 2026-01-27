package com.retro99.preferences.implementation.di

import org.koin.core.annotation.Module

/**
 * Platform-specific Koin module that provides the Settings.Factory implementation.
 *
 * Uses expect/actual pattern with @Module annotation so KSP can process
 * platform-specific implementations and include them in the module graph.
 *
 * Each platform implementation defines its own provider function since
 * Android requires Context while iOS doesn't need any dependencies.
 */
@Module
expect class PlatformPreferencesModule()

