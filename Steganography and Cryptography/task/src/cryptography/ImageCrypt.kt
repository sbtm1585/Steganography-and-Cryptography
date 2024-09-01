package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.experimental.xor

const val SUFFIX = "000000000000000000000011"

class ImageCrypt {
    fun hide() {
        try {
            val input = println("Input image file:").run { File(readln()) }
            val output = println("Output image file:"). run { File(readln()) }
            val msg = println("Message to hide:").run { readln() }
            val pw = println("Password:").run { readln() }
            val modPW = modifyPassword(msg, pw)
            val msgBA = messageToArray(msg)
            val pwBA = messageToArray(modPW)
            val encodedMsg = codeMessage(msgBA, pwBA)
            val image: BufferedImage = ImageIO.read(input)
            val checkSize = checkSize(encodedMsg, (image.height * image.width))
            if (!checkSize) {
                println("The input image is not large enough to hold this message")
                return
            }
            val byteString = buildString {
                for (byte in encodedMsg) {
                    append(byte.toString(2).padStart(8, '0'))
                }
            }
            var i = 0
            loop@for (y in 0 until image.width) {
                for (x in 0 until image.height) {
                    val c = Color(image.getRGB(x, y))
                    var b = c.blue
                    val string = b.toString(2)
                    val charArray = string.toCharArray().toMutableList()
                    charArray[charArray.lastIndex] = byteString[i]
                    b = charArray.joinToString("").toInt(2)
                    i++
                    image.setRGB(x, y, Color(c.red, c.green, b).rgb)
                    if (i > byteString.length - 1) break@loop
                }
            }
            ImageIO.write(image, "png", output)
            println("Message saved in ${output.name} image.")
        } catch (e: Exception) {
            println("Can't read input file!")
        }
    }

    fun show() {
        val input = println("Input image file:").run { File(readln()) }
        val pw = println("Password:").run { readln() }
        val image = ImageIO.read(input)
        val getMessage = mutableListOf<Char>()
        val compareList = MutableList(24) { '0' }
        getMessage.apply {
            loop@for (y in 0 until image.width) {
                for (x in 0 until image.height) {
                    val c = Color(image.getRGB(x, y))
                    val b = c.blue
                    compareList.add(b.toString(2).last())
                    compareList.removeAt(0)
                    add(b.toString(2).last())
                    if (SUFFIX == compareList.joinToString("")) {
                        break@loop
                    }
                }
            }
        }
        val decoded = getMessage.chunked(8).toMutableList()
        val byteArray = byteArrayOf().toMutableList()
        decoded.forEach { byteArray.add(it.joinToString("").toByte(2)) }
        val hiddenMsg = decoded.flatten().joinToString("")
        val pwBA = messageToArray(pw)
        val pwByteString = StringBuilder()
        for (byte in pwBA) {
            pwByteString.append(byte.toString(2).padStart(8, '0'))
        }
        val modPW = modifyPassword(hiddenMsg, pwByteString.toString())
        val pwByteArray = byteArrayOf().toMutableList()
        val pwArray = modPW.chunked(8).toMutableList()
        pwArray.forEach { pwByteArray.add(it.toByte(2)) }
        val decode = codeMessage(byteArray.toByteArray(), pwByteArray.toByteArray())
        val decodedMsg = arrayToMessage(decode)
        println("Message:\n$decodedMsg")
    }
    // if password is shorter or longer than the message it will be extended or shortened
    private fun modifyPassword(msg: String, pw: String): String {
        var counter = 0
        var x = ""
        if (msg.contains(SUFFIX)) x = SUFFIX
        if (pw.length > msg.length - x.length) {
            return pw.toCharArray().toMutableList().subList(0, msg.length - x.length).joinToString("") + SUFFIX
        }
        return buildString {
          while (msg.length - x.length > this.length) {
                this.append(pw[counter])
                counter ++
                if (counter > pw.length - 1) counter = 0
            }
            append(x)
        }
    }
    // the message will be de- or encoded and returned as a List of Bytes
    private fun codeMessage(msg: ByteArray, pw: ByteArray): ByteArray {
        val byteArray = mutableListOf<Byte>()
        return byteArray.apply {
            for (i in msg.indices) {
                val newByte = msg[i].xor(pw[i])
                add(newByte)
            }
            if (!msg.toList().containsAll(listOf(0b0, 0b0, 0b11))) addAll(listOf(0b0, 0b0, 0b11))
        }.toByteArray()
    }

    private fun arrayToMessage(array: ByteArray): String {
        val decoded = array.decodeToString(0, array.lastIndex - 2)
        return decoded
    }

    private fun messageToArray(msg: String): ByteArray {
        return msg.encodeToByteArray()
    }

    private fun checkSize(array: ByteArray, space: Int): Boolean {
        var sum = 0
        array.forEach { sum += it.toString(2).length }
        return sum <= space
    }
}