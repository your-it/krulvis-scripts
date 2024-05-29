package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement

private const val MOUNTED_XERICS_WIDGET = 187
private const val MOUNTED_XERICS_COMPONENT = 3

enum class MountedXericsTeleport(override val action: String) : OpenableHouseTeleport {
	XericsLookout("Xeric's Lookout"),
	XericsGlade("Xeric's Glade"),
	XericsInferno("Xeric's Inferno"),
	XericsHeart("Xeric's Heart"),
	XericsHonour("Xeric's Honour");

	override val requirements: List<Requirement> = emptyList()
	override val COMP_ID: Int = MOUNTED_XERICS_WIDGET
	override val WIDGET_ID: Int = MOUNTED_XERICS_COMPONENT
	override val OBJECT_NAMES = arrayOf("Xeric's Talisman")

	companion object {
		fun forName(name: String) = JewelleryBoxTeleport.values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}

}