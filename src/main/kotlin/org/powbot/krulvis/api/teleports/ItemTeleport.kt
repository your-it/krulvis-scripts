package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.TeleportItem

enum class ItemTeleport(val teleportItem: TeleportItem, override val action: String) : Teleport {
    GLORY_EDGEVILLE(TeleportItem.GLORY, "Edgeville"),
    ;

    override val requirements: List<Requirement> = teleportItem.requirements

    override fun execute(): Boolean {
        return teleportItem.teleport(action)
    }

    companion object {
        fun forName(name: String): ItemTeleport? {
            return ItemTeleport.values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
        }
    }
}

