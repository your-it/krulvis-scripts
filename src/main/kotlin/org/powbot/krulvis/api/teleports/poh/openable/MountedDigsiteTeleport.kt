package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement


private const val MOUNTED_DIGSITE_WIDGET = 187
private const val MOUNTED_DIGSITE_COMPONENT = 3

enum class MountedDigsiteTeleport(override val action: String) : OpenableHouseTeleport {
	Digsite("Digsite"),
	FossilIsland("Fossil Island"),
	Lithren("Lithren");

	override val requirements: List<Requirement> = emptyList()
	override val OBJECT_NAMES = arrayOf("Digsite Pendant")
	override val WIDGET_ID: Int = MOUNTED_DIGSITE_WIDGET
	override val COMP_ID: Int = MOUNTED_DIGSITE_COMPONENT

	companion object {
		fun forName(name: String) = JewelleryBoxTeleport.values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}
}