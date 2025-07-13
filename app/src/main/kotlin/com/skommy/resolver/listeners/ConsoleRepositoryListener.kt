package com.skommy.resolver.listeners

import com.skommy.services.Logger
import org.eclipse.aether.RepositoryEvent
import org.eclipse.aether.RepositoryListener

// Repository listener for resolution events
class ConsoleRepositoryListener : RepositoryListener {
    override fun artifactDescriptorInvalid(event: RepositoryEvent) {
        Logger.println("Invalid artifact descriptor: ${event.artifact}")
    }

    override fun artifactDescriptorMissing(event: RepositoryEvent) {
        Logger.println("Missing artifact descriptor: ${event.artifact}")
    }

    override fun artifactResolving(event: RepositoryEvent) {
        Logger.println("Resolving artifact: ${event.artifact}")
    }

    override fun artifactResolved(event: RepositoryEvent) {
        Logger.println("Resolved artifact: ${event.artifact}")
    }

    override fun artifactDownloading(event: RepositoryEvent) {}
    override fun artifactDownloaded(event: RepositoryEvent) {}
    override fun metadataDownloading(event: RepositoryEvent?) {
    }

    override fun metadataDownloaded(event: RepositoryEvent?) {
    }

    override fun artifactInstalling(event: RepositoryEvent?) {
    }

    override fun artifactInstalled(event: RepositoryEvent?) {
    }

    override fun metadataInstalling(event: RepositoryEvent?) {
    }

    override fun metadataInstalled(event: RepositoryEvent?) {
    }

    override fun artifactDeploying(event: RepositoryEvent?) {
    }

    override fun artifactDeployed(event: RepositoryEvent?) {
        TODO("Not yet implemented")
    }

    override fun metadataDeploying(event: RepositoryEvent?) {
        TODO("Not yet implemented")
    }

    override fun metadataDeployed(event: RepositoryEvent?) {
        TODO("Not yet implemented")
    }

    override fun metadataInvalid(event: RepositoryEvent) {}
    override fun metadataResolving(event: RepositoryEvent) {}
    override fun metadataResolved(event: RepositoryEvent) {}
}
