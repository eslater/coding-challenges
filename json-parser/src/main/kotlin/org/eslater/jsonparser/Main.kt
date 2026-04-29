package org.eslater.jsonparser

import java.io.File

fun main() {
    val content = File("/dev/stdin").readText()        // entire file as String

    val name = "Kotlin"
    println("Hello, $name!")

    for (i in 1..5) {
        println("i = $i")
    }
}

