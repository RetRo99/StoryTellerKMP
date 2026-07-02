package com.retro99.analytics.implementation

import com.retro99.analytics.api.FileLogger

class WebFileLogger : FileLogger {
    override fun logException(throwable: Throwable, message: String?) {
        println("[Crash] ${message ?: ""} ${throwable.message}")
    }

    override fun log(tag: String, message: String) {
        println("[$tag] $message")
    }

    override fun getLogFilePath(): String = ""

    override fun clearLogs() {
    }

    override fun getLogContents(): String = ""
}
