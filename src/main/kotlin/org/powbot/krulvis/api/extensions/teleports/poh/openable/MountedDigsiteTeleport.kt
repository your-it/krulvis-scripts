package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.requirement.Requirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory


private const val MOUNTED_DIGSITE_WIDGET = 187
private const val MOUNTED_DIGSITE_COMPONENT = 3

enum class MountedDigsiteTeleport(override val action: String) : OpenableHouseTeleport {
	Digsite("Digsite"),
	FossilIsland("Fossil Island"),
	Lithren("Lithren");

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val OBJECT_NAMES = arrayOf("Digsite Pendant")
	override val WIDGET_ID: Int = MOUNTED_DIGSITE_WIDGET
	override val COMP_ID: Int = MOUNTED_DIGSITE_COMPONENT
	override fun toString(): String {
		return "MountedDigsiteTeleport($name)"
	}
	companion object {
		fun forName(name: String) = values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}
}