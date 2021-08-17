package org.powbot.krulvis.api.utils.resources

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.powbot.krulvis.api.utils.requirements.Requirement
import org.powbot.krulvis.api.utils.requirements.RequirementDeserializer

object ATGson {

    fun Gson(): Gson {
        val builder = GsonBuilder()
//        builder.registerTypeAdapter(
//            BufferedImage::class.java,
//            JsonSerializer { src: BufferedImage?, _: Type?, _: JsonSerializationContext? ->
//                val baos = ByteArrayOutputStream()
//                ImageIO.write(src, "png", baos)
//                JsonPrimitive(
//                    Base64.getEncoder().encodeToString(baos.toByteArray())
//                )
//            }
//        )
//        builder.registerTypeAdapter(
//            BufferedImage::class.java,
//            JsonDeserializer { jsonElement, _, _ ->
//                val bytes = Base64.getDecoder().decode(jsonElement.asString)
//                val bis = ByteArrayInputStream(bytes)
//                ImageIO.read(bis)
//            }
//        )
        builder.registerTypeAdapter(
            Requirement::class.java,
            RequirementDeserializer()
        )
        return builder.create()
    }
}