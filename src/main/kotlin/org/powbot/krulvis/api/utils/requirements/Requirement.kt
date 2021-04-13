package org.powbot.krulvis.api.utils.requirements

import com.google.gson.Gson
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.powbot.krulvis.api.ATContext
import java.lang.reflect.Type

interface Requirement {
    fun hasRequirement(ctx: ATContext): Boolean

    fun List<Requirement>.hasAll(ctx: ATContext): Boolean {
        return all { it.hasRequirement(ctx) }
    }
}

class RequirementDeserializer : JsonDeserializer<Requirement> {
    override fun deserialize(p0: JsonElement?, p1: Type?, p2: JsonDeserializationContext?): Requirement {
        val jo = p0!!.asJsonObject
        if (jo.has("onlyBest")) {
            return Gson().fromJson(jo, InventoryRequirement::class.java)
        } else if (jo.has("quest")) {
            return Gson().fromJson(jo, QuestRequirement::class.java)
        } else {
            return Gson().fromJson(jo, EquipmentRequirement::class.java)
        }

    }
}