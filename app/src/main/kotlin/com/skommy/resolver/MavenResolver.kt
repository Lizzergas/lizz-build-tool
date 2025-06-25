package com.skommy.resolver

import com.skommy.resolver.listeners.ConsoleRepositoryListener
import com.skommy.resolver.listeners.ConsoleTransferListener
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.supplier.SessionBuilderSupplier
import org.eclipse.aether.util.graph.visitor.NodeListGenerator
import org.eclipse.aether.util.graph.visitor.PreorderDependencyNodeConsumerVisitor
import java.io.File

class MavenResolver(private val projectRoot: File = File(System.getProperty("user.dir"))) {
    private val repositorySystem: RepositorySystem = RepositorySystemSupplier().repositorySystem
    private val session: RepositorySystemSession = getRepositorySession(repositorySystem)
    private val central = RemoteRepository.Builder(
        "central", "default", "https://repo.maven.apache.org/maven2/"
    ).build()

    /**
     * Resolves a single artifact and returns its classpath.
     * @param coordinates Maven coordinates (groupId:artifactId:version)
     * @return Classpath string for the resolved artifact and its dependencies
     */
    fun resolveArtifact(coordinates: String): String {
        println("Resolving dependency: $coordinates")

        try {
            val artifact = DefaultArtifact(coordinates)
            val dependency = Dependency(artifact, "compile")

            val collectRequest = CollectRequest()
            collectRequest.setRoot(dependency)
            collectRequest.addRepository(central)
            val node = repositorySystem.collectDependencies(session, collectRequest).root

            val dependencyRequest = DependencyRequest()
            dependencyRequest.root = node

            repositorySystem.resolveDependencies(session, dependencyRequest)

            val nodeListGenerator = NodeListGenerator()
            val nlg = PreorderDependencyNodeConsumerVisitor(nodeListGenerator)
            node.accept(nlg)

            println("✓ Resolved $coordinates")
            return nodeListGenerator.classPath
        } catch (e: Exception) {
            println("✗ Failed to resolve $coordinates: ${e.message}")
            return ""
        }
    }

    /**
     * Resolves all artifacts and returns a combined list of all dependency paths.
     * This is more efficient than resolving one by one as it handles shared dependencies better.
     * @param coordinatesList List of Maven coordinates to resolve
     * @return List of all resolved JAR file paths (deduplicated)
     */
    fun resolveAll(coordinatesList: List<String>): List<String> {
        if (coordinatesList.isEmpty()) {
            return emptyList()
        }

        val allClasspaths = mutableSetOf<String>()

        coordinatesList.forEach { coordinates ->
            val classpath = resolveArtifact(coordinates)
            if (classpath.isNotBlank()) {
                classpath.split(File.pathSeparator)
                    .filter { it.isNotBlank() }
                    .forEach { allClasspaths.add(it) }
            }
        }

        return allClasspaths.toList()
    }

    private fun getRepositorySession(repositorySystem: RepositorySystem): RepositorySystemSession {
        val buildDepsPath = File(projectRoot, "build/deps").toPath()
        val sessionBuilder = SessionBuilderSupplier(repositorySystem)
            .get()
            .withLocalRepositoryBaseDirectories(buildDepsPath)
            .setTransferListener(ConsoleTransferListener())
            .setRepositoryListener(ConsoleRepositoryListener())
        val session = sessionBuilder.build()

        return session
    }
}