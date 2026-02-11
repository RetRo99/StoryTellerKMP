package org.retro99.storyteller

interface Platform {
    val name: String
    val isEink: Boolean
}

expect fun getPlatform(): Platform