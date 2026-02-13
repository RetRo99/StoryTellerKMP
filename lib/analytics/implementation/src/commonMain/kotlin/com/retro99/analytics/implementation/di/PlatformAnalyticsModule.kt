package com.retro99.analytics.implementation.di

import org.koin.core.annotation.Module

/**
 * Platform-specific Koin module that provides the FileLogger implementation.
 *
 * Uses expect/actual pattern with @Module annotation so KSP can process
 * platform-specific implementations and include them in the module graph.
 *
 * Each platform implementation defines its own provider function since
 * Android requires Context while iOS doesn't need any dependencies.
 */
@Module
expect class PlatformAnalyticsModule()

