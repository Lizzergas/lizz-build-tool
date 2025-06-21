package com.skommy

import java.io.File

fun getCurrentFolderName(): String {
    return File(File(".").canonicalPath).name
}