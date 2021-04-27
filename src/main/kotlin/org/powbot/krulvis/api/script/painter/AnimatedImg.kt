package org.powbot.krulvis.api.script.painter

import java.awt.Image
import java.awt.Toolkit
import java.io.ByteArrayOutputStream
import java.io.InputStream
import javax.imageio.*
import javax.imageio.metadata.IIOMetadata
import javax.imageio.metadata.IIOMetadataNode

object AnimatedImg {

    fun readImgFromFile(inputStream: InputStream): Image? {
        try {
// Get GIF reader
            val reader = ImageIO.getImageReadersByFormatName("gif").next()

            // Give it the stream to decode from
            reader.input = ImageIO.createImageInputStream(inputStream)
            val numImages = reader.getNumImages(true)

            // Get 'metaFormatName'. Need first frame for that.
            val imageMetaData = reader.getImageMetadata(0)
            val metaFormatName = imageMetaData.nativeMetadataFormatName


            // Prepare streams for image encoding
            val baoStream = ByteArrayOutputStream()
            ImageIO.createImageOutputStream(baoStream).use { ios ->
                // Get GIF writer that's compatible with reader
                val writer: ImageWriter = ImageIO.getImageWriter(reader)
                // Give it the stream to encode to
                writer.output = ios
                writer.prepareWriteSequence(null)
                for (i in 0 until numImages) {
                    // Get input image
                    val frameIn = reader.read(i)

                    // Get input metadata
                    val root = reader.getImageMetadata(i).getAsTree(metaFormatName) as IIOMetadataNode

                    // Find GraphicControlExtension node
                    val nNodes = root.length
                    for (j in 0 until nNodes) {
                        val node = root.item(j) as IIOMetadataNode
                        if (node.nodeName.equals("GraphicControlExtension", true)) {
                            // Get delay value
                            val delay = node.getAttribute("delayTime")

                            if (i == numImages - 1) {
                                node.setAttribute("delayTime", "400")
                            } else {
                                node.setAttribute("delayTime", "15")
                            }
                            break
                        }
                    }

                    // Create output metadata
                    val metadata: IIOMetadata = writer.getDefaultImageMetadata(ImageTypeSpecifier(frameIn), null)
                    // Copy metadata to output metadata
                    metadata.setFromTree(metadata.nativeMetadataFormatName, root)

                    // Create output image
                    val frameOut = IIOImage(frameIn, null, metadata)

                    // Encode output image
                    writer.writeToSequence(frameOut, writer.defaultWriteParam)
                }
                writer.endWriteSequence()
            }

            // Create image using encoded data
            return Toolkit.getDefaultToolkit().createImage(baoStream.toByteArray())
        } catch (e: IIOException) {
            println("Error loading image...")
            return null
        }
    }
}
