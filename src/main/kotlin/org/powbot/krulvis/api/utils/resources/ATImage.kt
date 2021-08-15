package org.powbot.krulvis.api.utils.resources

import java.awt.Image
import java.io.File
import java.io.FileNotFoundException
import javax.imageio.ImageIO

class ATImage(relativeFile: File, url: String) : ATResource(relativeFile, url) {

    private var image: Image? = null

    override fun getFileLocation(): File = getResourceFolder().resolve("imgs").resolve(relativeFile)

//    fun getAnimatedImage(): Image? {
//        if (image == null) {
//            try {
//                image = AnimatedImg.readImgFromFile(getFile().inputStream())
//            } catch (fnfe: FileNotFoundException) {
//                fnfe.printStackTrace()
//            }
//        }
//        return image
//    }

    fun getImage(): Image? {
        if (image == null) {
            try {
                image = ImageIO.read(getFile())
            } catch (fnfe: FileNotFoundException) {
                fnfe.printStackTrace()
            }
        }
        return image
    }

    companion object {
        val SKULL_GIF = ATImage(
            File("skull.gif"),
            "https://www.dropbox.com/s/qob9h4g7lqqmhya/skull.gif?dl=1"
        )
        val SKULL_PNG = ATImage(
            File("skull.png"),
            "https://www.dropbox.com/s/60wrf0tyshxdlvu/skull.png?dl=1"
        )

    }

}