package com.skommy

object CompilerConstants {
    val stdLib = "${getKotlinHome()}/lib/kotlin-stdlib.jar"
    val currentUser: String = System.getProperty("user.name")
    val globalClasspaths = System.getProperty("java.class.path")
    const val buildFolder = "build"

    fun getKotlinHome(): String {
        return System.getenv("KOTLIN_HOME").split(":").first()
    }
}

object BuildConstants {
    const val MAIN_KT_CLASS = "MainKt"
    const val MAIN_KT = "main.kt"
    const val CONFIG_FILE = "lizz.yaml"
    const val PROJECT_VERSION = "0.0.1"
    const val KOTLIN_VERSION = "2.2.0"
}