package org.powbot.krulvis.api.utils

import com.google.gson.JsonParser
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.InputStreamReader
import java.net.URL
import java.util.*
import java.util.Base64
import javax.imageio.ImageIO


object Utils {

    private val OSRS_BOX_URL = "https://api.osrsbox.com/equipment/"

    fun sleep(delay: Int) {
        Thread.sleep(delay.toLong())
    }

    fun sleep(min: Int, max: Int) {
        Thread.sleep(Random.nextInt(min, max).toLong())
    }

    fun short(): Int = Random.nextInt(600, 1000)

    fun mid(): Int = Random.nextInt(1000, 2500)

    fun long(): Int = Random.nextInt(2500, 5000)

    fun waitFor(min: Int, max: Int, condition: () -> Boolean): Boolean {
        return waitFor(
            Random.nextInt(
                min,
                max
            ), condition
        )
    }

    fun waitFor(timeOut: Int = mid(), condition: () -> Boolean): Boolean {
        val totalDelay = System.currentTimeMillis() + timeOut
        do {
            if (condition.invoke()) {
                return true
            }
            sleep(
                Random.nextInt(
                    150,
                    250
                )
            )
        } while (totalDelay > System.currentTimeMillis())
        return false
    }


//    fun getItemImage(id: Int): BufferedImage? {
//        try {
//            val conn = URL(OSRS_BOX_URL + id).openConnection()
//            val parser = JsonParser()
//            val ipr = InputStreamReader(conn.getInputStream())
//            val icon = parser.parse(ipr)?.asJsonObject?.get("icon")?.asString ?: return null
//            return ImageIO.read(ByteArrayInputStream(Base64.getDecoder().decode(icon)))
//        } catch (fnfe: FileNotFoundException) {
//            fnfe.printStackTrace()
//        }
//        return null
//    }
//
//    fun getWebImage(url: String): BufferedImage? {
//        try {
//            val conn = URL(url).openConnection()
//            return ImageIO.read(conn.getInputStream())
//        } catch (fnfe: FileNotFoundException) {
//            fnfe.printStackTrace()
//        }
//        return null
//    }
}