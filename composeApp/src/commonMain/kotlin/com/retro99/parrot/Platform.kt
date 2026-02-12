package com.retro99.parrot

interface Platform {
    val name: String
    val isEink: Boolean
}

expect fun getPlatform(): Platform

