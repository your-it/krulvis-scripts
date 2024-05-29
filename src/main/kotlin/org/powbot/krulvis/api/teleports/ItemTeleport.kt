package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.ItemRequirement
import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.TeleportItem

const val EDGEVILLE_GLORY = "Edgeville glory"

enum class ItemTeleport(val teleportItem: TeleportItem, override val action: String) : Teleport {
	EDGEVILLE_GLORY(TeleportItem.GLORY, "Edgeville"),
	;

	override val requirements: List<Requirement> = listOf(ItemRequirement(teleportItem.ids, 1, ItemRequirement.ItemRequirementType.EITHER))

	override fun execute(): Boolean {
		return teleportItem.teleport(action)
	}

	companion object {
		fun forName(name: String): ItemTeleport? {
			return ItemTeleport.values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
		}
	}
}

