package org.powbot.krulvis.api.utils

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets


fun main() {
    println(Discord.upload("https://i.imgur.com/gXKGZeT.png"))
}

object Discord {

    //Authorization: Client-ID df8cd43125c36c8
//    val clientId = "df8cd43125c36c8"
    val clientId = "0a12cf62d9839bd"


    fun upload(link: String): String {
        try {
            val conn =
                URL("https://discord.com/api/webhooks/788073059495837706/mAllbOd2zCCnnqRolL7WrJA5iCMJt_YPwlgx6YPr158zZFavnnRM9kZqLEgxwNEhHSKR").openConnection() as HttpURLConnection
            conn.addRequestProperty("Content-Type", "application/json")
            conn.doOutput = true
            conn.requestMethod = "POST"
            val json = JsonObject()
            val embeds = JsonArray()
            val embed = JsonObject()
            val image = JsonObject()
            JsonObject()
            image.add("url", JsonPrimitive(link))
            embed.add("image", image)
            embeds.add(embed)
            json.add("embeds", embeds)

            //Send data
            val stream = conn.outputStream
            val data = json.toString().toByteArray(StandardCharsets.UTF_8)
//            println("Json: $json")
            stream.write(data)
            stream.flush()
            stream.close()

            //Get response
            conn.inputStream.close()
            conn.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return "no response"
    }
}