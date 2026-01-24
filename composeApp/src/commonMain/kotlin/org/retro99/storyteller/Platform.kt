package org.retro99.storyteller

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform