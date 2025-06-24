package com.skommy.commands

import com.github.ajalt.clikt.core.CliktCommand
import org.eclipse.aether.RepositoryEvent
import org.eclipse.aether.RepositoryListener
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.supplier.SessionBuilderSupplier
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transfer.TransferListener
import org.eclipse.aether.util.graph.visitor.PreorderNodeListGenerator
import java.nio.file.Path

class Sync : CliktCommand() {
    override fun run() {
        val repoSystem = RepositorySystemSupplier().repositorySystem
        val session = getRepositorySession(repoSystem)

        val artifact = DefaultArtifact("com.google.code.gson:gson:2.13.1")
        val dependency = Dependency(artifact, "compile")
        val central =
            RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2/").build()

        val collectRequest = CollectRequest()
        collectRequest.setRoot(dependency)
        collectRequest.addRepository(central)
        val node = repoSystem.collectDependencies(session, collectRequest).root

        val dependencyRequest = DependencyRequest()
        dependencyRequest.root = node

        repoSystem.resolveDependencies(session, dependencyRequest)

        val nlg = PreorderNodeListGenerator()
        node.accept(nlg)
        println("PATH: ${nlg.classPath}")
    }

    fun getRepositorySession(repositorySystem: RepositorySystem): RepositorySystemSession {
        val sessionBuilder = SessionBuilderSupplier(repositorySystem)
            .get()
            .withLocalRepositoryBaseDirectories(Path.of("build/deps"))
            .setTransferListener(ConsoleTransferListener())
            .setRepositoryListener(ConsoleRepositoryListener())
        val session = sessionBuilder.build()

        return session
    }
}

// Transfer listener for download progress
class ConsoleTransferListener : TransferListener {
    override fun transferInitiated(event: TransferEvent) {
        val resource = event.resource
        println("Downloading ${resource.repositoryUrl}${resource.resourceName}")
    }

    override fun transferProgressed(event: TransferEvent) {
        // Could implement progress bar here if needed
    }

    override fun transferSucceeded(event: TransferEvent) {
        val resource = event.resource
        val size = event.transferredBytes
        println("Downloaded ${resource.resourceName} (${size / 1024} KB)")
    }

    override fun transferFailed(event: TransferEvent) {
        val resource = event.resource
        println("Failed to download ${resource.resourceName}: ${event.exception?.message}")
    }

    override fun transferCorrupted(event: TransferEvent) {
        val resource = event.resource
        println("Corrupted download ${resource.resourceName}: ${event.exception?.message}")
    }

    override fun transferStarted(event: TransferEvent) {}
}

// Repository listener for resolution events
class ConsoleRepositoryListener : RepositoryListener {
    override fun artifactDescriptorInvalid(event: RepositoryEvent) {
        println("Invalid artifact descriptor: ${event.artifact}")
    }

    override fun artifactDescriptorMissing(event: RepositoryEvent) {
        println("Missing artifact descriptor: ${event.artifact}")
    }

    override fun artifactResolving(event: RepositoryEvent) {
        println("Resolving artifact: ${event.artifact}")
    }

    override fun artifactResolved(event: RepositoryEvent) {
        println("Resolved artifact: ${event.artifact}")
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