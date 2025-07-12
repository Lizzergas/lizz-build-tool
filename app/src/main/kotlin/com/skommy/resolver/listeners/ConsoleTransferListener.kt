package com.skommy.resolver.listeners

import com.skommy.services.LoggerProvider
import com.skommy.services.LoggerService
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener

// Transfer listener for download progress
class ConsoleTransferListener(
    private val logger: LoggerService = LoggerProvider.get()
) : TransferListener {
    override fun transferInitiated(event: TransferEvent) {
        val resource = event.resource
        logger.println("Downloading ${resource.repositoryUrl}${resource.resourceName}")
    }

    override fun transferProgressed(event: TransferEvent) {
        // Could implement progress bar here if needed
    }

    override fun transferSucceeded(event: TransferEvent) {
        val resource = event.resource
        val size = event.transferredBytes
        logger.println("Downloaded ${resource.resourceName} (${size / 1024} KB)")
    }

    override fun transferFailed(event: TransferEvent) {
        val resource = event.resource
        logger.println("Failed to download ${resource.resourceName}: ${event.exception?.message}")
    }

    override fun transferCorrupted(event: TransferEvent) {
        val resource = event.resource
        logger.println("Corrupted download ${resource.resourceName}: ${event.exception?.message}")
    }

    override fun transferStarted(event: TransferEvent) {}
}