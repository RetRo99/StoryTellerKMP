package com.retro99.base.annotations

import org.koin.core.annotation.Scope

/**
 * Scope annotation for Activity-scoped dependencies.
 * Use with Koin's scope system.
 */
@Scope(name = "activity")
annotation class ActivityScope
