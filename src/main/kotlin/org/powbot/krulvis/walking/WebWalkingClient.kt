package org.powbot.krulvis.walking

import com.google.gson.*
import org.powbot.walking.model.*
import org.powerbot.bot.RestClient
import org.powerbot.bot.ResultType
import java.lang.RuntimeException
import java.lang.reflect.Type


internal class EdgeDeserializer<T> : JsonDeserializer<T> {

    companion object {
        private const val TypeFieldName = "type"
    }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): T {
        val jsonObj = json.asJsonObject
        val className = jsonObj[TypeFieldName].asString

        val clazz = when (className) {
            EdgeType.Tile.name -> TileEdge::class.java
            EdgeType.GameObject.name -> GameObjectEdge::class.java
            EdgeType.Item.name -> ItemEdge::class.java
            EdgeType.Npc.name -> NpcEdge::class.java
            EdgeType.Spell.name -> SpellEdge::class.java
            else -> throw RuntimeException("Unsupported type $className")
        }
        return context.deserialize(json, clazz)
    }
}

class WebWalkingClient : RestClient("/api/v1/walking") {

    companion object {
        val instance: WebWalkingClient = WebWalkingClient()
    }

    fun getPath(req: GeneratePathRequest): List<Edge<*>>? {
        return executePost("/generate", req, Edge::class.java, ResultType.LIST)
            ?: emptyList()
    }
}
