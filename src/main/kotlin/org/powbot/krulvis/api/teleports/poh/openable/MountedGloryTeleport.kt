package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement

const val EDGEVILLE_MOUNTED_GLORY = "Edgeville mounted glory (poh)"

enum class MountedGloryTeleport(override val action: String) : OpenableHouseTeleport {
	Edgeville("Edgeville"),
	Karamja("Karamja"),
	DraynorVillage("Draynor Village"),
	AlKharid("Al Kharid");

	override val requirements: List<Requirement> = emptyList()
	override val OBJECT_NAMES = arrayOf("Amulet of Glory")
	override val WIDGET_ID: Int = -1
	override val COMP_ID: Int = -1

	companion object {
		fun forName(name: String) = JewelleryBoxTeleport.values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}
}