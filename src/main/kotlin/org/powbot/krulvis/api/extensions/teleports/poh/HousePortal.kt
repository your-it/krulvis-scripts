package org.powbot.krulvis.api.extensions.teleports.poh

import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val LUNAR_ISLE_HOUSE_PORTAL = "Lunar isle portal POH"

enum class HousePortal : HouseTeleport {
	ARCEUUS_LIBRARY,
	DRAYNOR_MANOR,
	BATTLEFRONT,
	VARROCK,
	MIND_ALTAR,
	LUMBRIDGE,
	FALADOR,
	SALVE_GRAVEYARD,
	CAMELOT,
	FENKENSTRAIN,
	KOUREND_CASTLE,
	EAST_ARDOUGNE,
	CIVITAS_ILLA_FORTIS,
	WATCHTOWER,
	DIGSITE,
	WEST_ARDOUGNE,
	MARIM,
	HARMONY_ISLAND,
	CANIFIS,
	LUNAR_ISLE,
	FORGOTTEN_CEMETERY,
	WATERBIRTH_ISLAND,
	BARROWS,
	GRAVEYARD_OF_SHADOWS,
	FISHING_GUILD,
	CATHERBY,
	DEMONIC_RUINS,
	APE_ATOLL_DUNGEON,
	FROZEN_WASTE_PLATEAU,
	TROLL_STRONGHOLD,
	WEISS
	;

	override val logger: Logger = LoggerFactory.getLogger("HousePortal")
	override val action: String = "Enter"
	override val requirements: List<Requirement> = emptyList()

	private val portal: GameObject
		get() = Objects.stream().type(GameObject.Type.INTERACTIVE)
			.nameContains(name.replace("_", " "))
			.nameContains("portal")
			.action("Enter").first()

	override fun insideHouseTeleport(): Boolean {
		return walkAndInteract(portal, "Enter")
	}

	companion object {
		fun forName(portalName: String): HousePortal? {
			return if (!portalName.contains("portal POH")) null
			else values().firstOrNull { portalName.contains(it.name.replace("_", " "), true) }
		}
	}

}

fun main() {
	println(HousePortal.forName(LUNAR_ISLE_HOUSE_PORTAL))
}
