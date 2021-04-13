package org.powbot.krulvis.api.utils.resources

import java.awt.Font
import java.io.File

class ATFont(relativeFile: File, url: String) : ATResource(relativeFile, url) {

    private var font: Font? = null

    fun getFont(): Font? {
        if (font == null) {
            font = Font.createFont(
                Font.TRUETYPE_FONT,
                getFile()
            )
        }
        return font
    }

    override fun getFileLocation(): File = getResourceFolder().resolve("fonts").resolve(relativeFile)


    companion object {
        val HEIDH_FONT = ATFont(
            File("heidh.ttf"),
            "https://www.dropbox.com/s/d9tcuvl60s43seu/heidh.ttf?dl=1"
        )
        val FIRE_FLIGHT_FONT = ATFont(File("Fire Flight.ttf"), "https://")
        val KAGATSUN_FONT = ATFont(File("Kagatsun.ttf"), "https://")
        val RUNESCAPE_FONT = ATFont(
            File("Runescape_UF.ttf"),
            "https://www.dropbox.com/s/0neu8hzsw3s2c57/Runescape_UF.ttf?dl=1"
        )
    }

}