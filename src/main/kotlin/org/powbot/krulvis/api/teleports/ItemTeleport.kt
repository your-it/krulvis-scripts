package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.ItemRequirement
import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val EDGEVILLE_GLORY = "Edgeville glory"
const val FEROX_ENCLAVE_ROD = "Ferox Enclave ROD"
const val CASTLE_WARS_ROD = "Castle Wars ROD"
const val FREMENNIK_SLAYER = "Fremennik Slayer Ring"
const val STRONGHOLD_SLAYER = "Stronghold Slayer Ring"

enum class ItemTeleport(val teleportItem: TeleportItem, override val action: String) : Teleport {
	EDGEVILLE_GLORY(TeleportItem.GLORY, "Edgeville"),
	FEROX_ENCLAVE_ROD(TeleportItem.ROD, "Ferox Enclave"),
	CASTLE_WARS_ROD(TeleportItem.ROD, "Castle Wars"),
	FREMENNIK_SLAYER(TeleportItem.SLAYER, "Fremennik"),
	STRONGHOLD_SLAYER(TeleportItem.SLAYER, "Stronghold"),
	;

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = listOf(ItemRequirement(teleportItem.ids, 1, ItemRequirement.ItemRequirementType.EITHER))

	override fun execute(): Boolean {
		return teleportItem.teleport(action)
	}

	override fun toString(): String {
		return "ItemTeleport($name)"
	}

	companion object {
		fun forName(name: String): ItemTeleport? {
			return ItemTeleport.values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
		}
	}
}

