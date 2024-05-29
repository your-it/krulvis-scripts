package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.ScrollHelper
import org.powbot.api.rt4.Widgets

private const val PORTAL_NEXUS = "Portal Nexus"
private const val PORTAL_WIDGET = 17
private const val PORTAL_COMPONENT = 12
private const val PORTAL_CLOSE_BUTTON_COMPONENT = 1
private const val PORTAL_CLOSE_BUTTON = 13


const val FALADOR_TELEPORT_NEXUS = "Falador nexus teleport (poh)"

enum class NexusPortalTeleport(override val action: String) : OpenableHouseTeleport {
	ArceuusLibrary("Arceuus Library"),
	DraynorManor("Draynor Manor"),
	Battlefront("Battlefront"),
	Varrock("Varrock"),
	GrandExchange("Grand Exchange"),
	MindAltar("Mind Altar"),
	Lumbridge("Lumbridge"),
	Falador("Falador"),
	SalveGraveyard("Salve Graveyard"),
	Camelot("Camelot"),
	SeersVillage("Seers' Village"),
	FenkenstrainsCastle("Fenkenstrain's Castle"),
	EastArdougne("East Ardougne"),
	Watchtower("Watchtower"),
	Yanille("Yanille"),
	Senntisten("Senntisten"),
	WestArdougne("West Ardougne"),
	Marim("Marim"),
	HarmonyIsland("Harmony Island"),
	Kharyrll("Kharyrll"),
	KourendCastle("Kourend Castle"),
	LunarIsle("Lunar Isle"),
	ForgottenCemetery("The Forgotten Cemetery"),
	WaterbirthIsland("Waterbirth Island"),
	Barrows("Barrows"),
	Carrallanger("Carrallanger"),
	FishingGuild("Fishing Guild"),
	Catherby("Catherby"),
	Annakarl("Annakarl"),
	ApeAtollDungeon("Ape Atoll Dungeon"),
	Ghorrock("Ghorrock"),
	Weiss("Weiss"),
	TrollStronghold("Troll Stronghold");

	override val WIDGET_ID: Int = PORTAL_WIDGET
	override val COMP_ID: Int = PORTAL_COMPONENT
	override val OBJECT_NAMES = arrayOf(PORTAL_NEXUS)
	override val requirements: List<Requirement> = emptyList()
	private fun scrollComponent(): Component = Widgets.component(WIDGET_ID, 14)
	private fun paneComponent(): Component = Widgets.component(WIDGET_ID, 11)

	override fun scroll(): Boolean {
		val teleportComp = getTeleportComponent()
		val paneComp = paneComponent()
		val scrollComp = scrollComponent()
		return ScrollHelper.scrollTo({ teleportComp }, { paneComp }, { scrollComp })
	}

	companion object {
		fun forName(name: String) = JewelleryBoxTeleport.values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}
}