package com.retro99.analytics.api

/**
 * Interface for logging to a file on disk.
 * Allows users to share log files for debugging purposes.
 */
interface FileLogger {

    /**
     * Logs an exception with an optional context message to the log file.
     *
     * @param throwable The exception to log
     * @param message Optional context message
     */
    fun logException(throwable: Throwable, message: String?)

    /**
     * Logs a general message to the log file.
     *
     * @param tag The tag/category for the log entry
     * @param message The message to log
     */
    fun log(tag: String, message: String)

    /**
     * Gets the path to the current log file.
     * Users can share this file for debugging.
     *
     * @return The absolute path to the log file
     */
    fun getLogFilePath(): String

    /**
     * Clears the log file contents.
     */
    fun clearLogs()

    /**
     * Gets the contents of the log file as a string.
     *
     * @return The log file contents, or empty string if file doesn't exist
     */
    fun getLogContents(): String
}

