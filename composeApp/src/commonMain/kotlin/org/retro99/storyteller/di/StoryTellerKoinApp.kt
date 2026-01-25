package org.retro99.storyteller.di

import org.koin.core.annotation.KoinApplication

/**
 * KoinApplication annotation that triggers KSP to generate
 * startKoin() and koinApplication() functions.
 * 
 * All @Configuration annotated modules (AppModule, etc.) will be
 * automatically discovered and included.
 */
@KoinApplication
class StoryTellerKoinApp

