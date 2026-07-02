package com.retro99.parrot

class WasmPlatform : Platform {
    override val name: String = "Web"
    override val isEink: Boolean = false
}

actual fun getPlatform(): Platform = WasmPlatform()
