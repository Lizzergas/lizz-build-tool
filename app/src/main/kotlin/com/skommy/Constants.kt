package com.skommy

object Constants {
    val ktHome: String? = System.getenv("KOTLIN_HOME")
    val stdLib = "$ktHome/lib/kotlin-stdlib.jar"
    val globalClasspaths = System.getProperty("java.class.path")
    val currentUser = System.getProperty("user.name")
}