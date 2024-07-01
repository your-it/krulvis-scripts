package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val EDGEVILLE_MOUNTED_GLORY = "Edgeville mounted glory (poh)"

enum class MountedGloryTeleport(override val action: String) : OpenableHouseTeleport {
	Edgeville("Edgeville"),
	Karamja("Karamja"),
	DraynorVillage("Draynor Village"),
	AlKharid("Al Kharid");

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val OBJECT_NAMES = arrayOf("Amulet of Glory")
	override val WIDGET_ID: Int = -1
	override val COMP_ID: Int = -1
	override fun toString(): String {
		return "MountedGloryTeleport($name)"
	}

	companion object {
		fun forName(name: String): MountedGloryTeleport? {
			return if (!name.contains("mounted glory")) null
			else values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
		}
	}
}