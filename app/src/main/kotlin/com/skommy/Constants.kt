package com.skommy

import com.skommy.models.BuildSettings

object CompilerConstants {
    val stdLib = "${getKotlinHome()}/lib/kotlin-stdlib.jar"
    val currentUser: String = System.getProperty("user.name")
    val globalClasspaths = System.getProperty("java.class.path")
    const val buildFolder = "build"

    fun getKotlinHome(): String {
        return System.getenv("KOTLIN_HOME").split(":").first()
    }

    // New function to get Kotlin home from settings or fallback to environment
    fun getKotlinHome(settings: BuildSettings): String {
        return settings.kotlin.kotlinHome.takeIf { it.isNotBlank() } 
            ?: getKotlinHome()
    }

    // New function to get stdlib path from settings
    fun getStdLib(settings: BuildSettings): String {
        return "${getKotlinHome(settings)}/lib/kotlin-stdlib.jar"
    }
}

object BuildConstants {
    const val MAIN_KT_CLASS = "MainKt"
    const val MAIN_KT = "main.kt"
    const val CONFIG_FILE = "lizz.yaml"
    const val PROJECT_VERSION = "0.0.1"
    const val KOTLIN_VERSION = "2.2.0"
}
