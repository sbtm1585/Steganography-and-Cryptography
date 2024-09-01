package cryptography

import kotlin.system.exitProcess

fun main() {
    val c = ImageCrypt()
    while (true) {
        println("Task (hide, show, exit):")
        when(val input = readln()) {
            "exit" -> println("Bye!").also { exitProcess(0) }
            "hide" -> c.hide()
            "show" -> c.show()
            else -> println("Wrong task: $input")
        }
    }
}