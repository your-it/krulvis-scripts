package org.powbot.krulvis.api.utils

import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Base64
import javax.imageio.ImageIO

fun main() {
    val img = ImageIO.read(File("/home/krulvis/.powbot/proggies/Tempoross - Debug_12-04-2021_02-04-48.png"))
    println(Imgur.upload(img))
}

object Imgur {

    //Authorization: Client-ID df8cd43125c36c8
//    val clientId = "df8cd43125c36c8"
    val clientId = "0a12cf62d9839bd"


    fun upload(image: BufferedImage?): String {
        try {
            val baos = ByteArrayOutputStream()
            ImageIO.write(image, "png", baos)
            val url = URL("https://api.imgur.com/3/image")
            val conn = url.openConnection() as HttpURLConnection

            conn.setRequestProperty(
                "User-Agent",
                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2"
            );
            conn.addRequestProperty("Authorization", "Client-ID $clientId")
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            conn.doOutput = true
            conn.doInput = true
            conn.requestMethod = "POST"
            conn.connect()

            // Write image to connection
            val wr = OutputStreamWriter(conn.outputStream)
            val bytes = baos.toByteArray()
            val data =
                URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(
                    Base64.getEncoder().encodeToString(bytes),
                    "UTF-8"
                )
//            val data =
//                URLEncoder.encode("image", "UTF-8") + "=" + URLEncoder.encode(encode(bytes), "UTF-8")
            println(data)
            wr.write(data)
            wr.flush()

            //Get response
            val inputStream = if ((conn as HttpURLConnection).responseCode != 200) {
                println("Got error HTTP CODE: ${conn.responseCode}")
                conn.errorStream
            } else {
                println("Successful!")
                conn.inputStream
            }
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            var decodedString: String?
            while (bufferedReader.readLine().also { decodedString = it } != null) {
                println(decodedString);
                if (decodedString?.contains("link") == true) {
                    val output = decodedString!!
                    println("Link received from imgur: $output")
                    val start = "\"link\":\""
                    val end = ".png"
                    return output.substring(
                        output.indexOf(start) + start.length,
                        output.indexOf(end) + 4
                    ).replace("\\\\".toRegex(), "")
                }
            }
            baos.close()
            wr.close()
            bufferedReader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "no response"
    }
}