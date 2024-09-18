package org.powbot.krulvis.api.extensions.teleports

import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.IEquipmentItem
import org.powbot.krulvis.api.extensions.items.ITeleportItem
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val EDGEVILLE_GLORY = "Edgeville glory"
const val FEROX_ENCLAVE_ROD = "Ferox Enclave ROD"
const val CASTLE_WARS_ROD = "Castle Wars ROD"
const val FREMENNIK_SLAYER = "Fremennik Slayer Ring"
const val MORYTANIA_SLAYER = "Morytania Slayer Ring"
const val STRONGHOLD_SLAYER = "Stronghold Slayer Ring"
const val TEARS_OF_GUTHIX_GAMES = "Tears of Guthix Games"
const val MOUNT_KARUULM_BLESSING = "Mount Karuulm Blessing"
const val GWD_GHOMMAL_HILT = "GWD Ghommal Hilt"
const val ROYAL_SEED_POD = "Royal Seed Pod"
const val LAVA_MAZE_BURNING = "Lava Maze BA"
const val BANDIT_CAMP_BURNING = "Bandit Camp BA"

enum class ItemTeleport(
	val teleportItem: ITeleportItem,
	override val action: String,
	override val wildernessLevel: Int = 20
) : Teleport {
	EDGEVILLE_GLORY(TeleportEquipment.GLORY, "Edgeville", 30),
	FEROX_ENCLAVE_ROD(TeleportEquipment.ROD, "Ferox Enclave"),
	CASTLE_WARS_ROD(TeleportEquipment.ROD, "Castle Wars"),
	FREMENNIK_SLAYER_RING(TeleportEquipment.SLAYER, "Fremennik"),
	STRONGHOLD_SLAYER_RING(TeleportEquipment.SLAYER, "Stronghold"),
	MORYTANIA_SLAYER_RING(TeleportEquipment.SLAYER, "Morytania"),
	ROYAL_SEED_POD(TeleportItem.ROYAL_SEED_POD, "Commune", 30),
	TEARS_OF_GUTHIX_GAMES(TeleportEquipment.GAMES, "Tears of Guthix"),
	MOUNT_KARUULM_BLESSING(TeleportEquipment.RADAS_BLESSING, "Mount Karuulm"),
	GWD_GHOMMAL_HILT(TeleportEquipment.GHOMMAL_HILT, "Trollheim"),
	LAVA_MAZE_BA(TeleportEquipment.BURNING, "Lava Maze"),
	BANDIT_CAMP_BA(TeleportEquipment.BURNING, "Bandit Camp"),
	;

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> by lazy {
		if (teleportItem is IEquipmentItem) {
			listOf(EquipmentRequirement(teleportItem, 1))
		} else {
			listOf(InventoryRequirement(teleportItem, 1))
		}
	}

	override fun execute(): Boolean {
		return teleportItem.teleport(action)
	}

	override fun toString(): String {
		return "ItemTeleport(name=$name, action=$action)"
	}

	companion object {
		fun forName(name: String): ItemTeleport? {
			return ItemTeleport.values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
		}
	}
}

