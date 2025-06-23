package com.skommy

object CompilerConstants {
    val ktHome: String? = System.getenv("KOTLIN_HOME")
    val stdLib = "$ktHome/lib/kotlin-stdlib.jar"
    val currentUser: String = System.getProperty("user.name")
    val globalClasspaths = System.getProperty("java.class.path")
    const val buildFolder = "build"
}

object BuildConstants {
    const val MAIN_KT = "MainKt"
    const val PROJECT_VERSION = "0.0.1"
    const val KOTLIN_VERSION = "2.1.20"
}