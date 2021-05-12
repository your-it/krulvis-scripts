package org.powbot.krulvis.api.utils.resources

import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.net.URL

open class ATResource(val relativeFile: File, private val url: String) {

    open fun getFileLocation(): File = getResourceFolder().resolve(relativeFile)

    fun getFile(): File {
        val file = getFileLocation()
//        println("File location: $file")
        if (!file.exists()) {
            println("$file not found, have to download it first")
            file.parentFile.mkdirs()
            file.createNewFile()
            downloadFile()
        } else {
//            println("File is found at location")
        }
        return file
    }

    private fun downloadFile(): Boolean {
        val fos = FileOutputStream(getFileLocation())
        try {
            val conn = URL(url).openConnection()
            val ips = conn.getInputStream()
            fos.write(ips.readBytes())
            fos.flush()
            return true
        } catch (fnfe: FileNotFoundException) {
            fnfe.printStackTrace()
        } finally {
            fos.close()
        }
        return false
    }

    fun getResourceFolder(): File {
        val home = System.getProperty("user.home")
        val eb = home + File.separator + "EpicBot"
        val resourceFolder =
            File(eb + File.separator + "Script Settings" + File.separator + "ScriptResources" + File.separator)
        if (!resourceFolder.exists()) {
            println("Making resource folders!")
            resourceFolder.mkdirs()
        }
        return resourceFolder
    }

}