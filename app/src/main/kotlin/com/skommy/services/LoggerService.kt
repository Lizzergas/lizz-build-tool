package com.skommy.services

import com.github.ajalt.clikt.core.Context

/**
 * Interface for logging services in the Lizz build tool.
 * Provides methods for printing messages with proper formatting.
 */
interface LoggerService {
    fun println(message: String, err: Boolean = false)
    fun print(message: String, err: Boolean = false)
}

/**
 * Default implementation of LoggerService that uses Clikt's Context for proper CLI output.
 */
class DefaultLoggerService(private val currentContext: Context) : LoggerService {
    override fun println(message: String, err: Boolean) {
        currentContext.echoMessage(currentContext, message, false, err)
    }

    override fun print(message: String, err: Boolean) {
        currentContext.echoMessage(currentContext, message, false, err)
    }
}

/**
 * Provider for LoggerService instances.
 * Allows setting and getting the current logger implementation.
 */
object LoggerProvider {
    private var loggerService: LoggerService? = null

    fun set(logger: LoggerService) {
        this.loggerService = logger
    }

    fun get(): LoggerService {
        return loggerService ?: throw IllegalStateException("Logger not initialized")
    }
}
