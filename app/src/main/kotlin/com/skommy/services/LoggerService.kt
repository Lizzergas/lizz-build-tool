package com.skommy.services

import com.github.ajalt.clikt.core.Context

/**
 * Singleton Logger for the Lizz build tool.
 * Provides global access to logging functionality with proper CLI output formatting.
 */
object Logger {
    private var currentContext: Context? = null

    /**
     * Initialize the logger with a Clikt Context for proper CLI output.
     * This should be called once at application startup.
     */
    fun initialize(context: Context) {
        this.currentContext = context
    }

    /**
     * Print a message with a newline.
     */
    fun println(message: String, err: Boolean = false) {
        val context = currentContext ?: throw IllegalStateException("Logger not initialized. Call Logger.initialize() first.")
        context.echoMessage(context, message, false, err)
    }

    /**
     * Print a message without a newline.
     */
    fun print(message: String, err: Boolean = false) {
        val context = currentContext ?: throw IllegalStateException("Logger not initialized. Call Logger.initialize() first.")
        context.echoMessage(context, message, false, err)
    }
}

