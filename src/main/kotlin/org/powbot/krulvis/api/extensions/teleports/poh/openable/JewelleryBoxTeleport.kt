package org.powbot.krulvis.api.extensions.teleports.poh.openable

import org.powbot.api.requirement.Requirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val JEWELLERYBOX_WIDGET = 590
private const val JEWELLERYBOX_COMPONENT = 1
private const val JEWELLERYBOX_CLOSE_BUTTON = 11

private const val BASIC_JEWELLERY_BOX = "Basic jewellery box"
private const val FANCY_JEWELLERY_BOX = "Fancy jewellery box"
private const val ORNATE_JEWELLERY_BOX = "Ornate jewellery box"

const val PVP_ARENA_JEWELLERY_BOX = "PvP arena jewelry box (POH)"
const val CASTLE_WARS_JEWELLERY_BOX = "Castle wars jewelry box (POH)"
const val FEROX_ENCLAVE_JEWELLERY_BOX = "Ferox enclave jewelry box (POH)"

enum class JewelleryBoxTeleport(override val action: String, override val COMP_ID: Int) : OpenableHouseTeleport {
	PVP_ARENA("PvP Arena", 2),
	CASTLE_WARS("Castle Wars", 2),
	FEROX_ENCLAVE("Ferox Enclave", 2),
	BURTHORPE("Burthorpe", 3),
	BARBARIAN_OUTPOST("Barbarian Outpost", 3),
	CORPORAL_BEAST("Corporeal Beast", 3),
	TEARS_OF_GUTHIX("Tears of Guthix", 3),
	WINTERTODT("Wintertodt Camp", 3),
	WARRIORS_GUILD("Warriors' Guild", 4),
	CHAMPIONS_GUILD("Champions' Guild", 4),
	MONASTERY("Monastery", 3),
	RANGING_GUILD("Ranging Guild", 4),
	FISHING_GUILD("Fishing Guild", 5),
	MINING_GUILD("Mining Guild", 5),
	CRAFTING_GUILD("Crafting Guild", 5),
	COOKING_GUILD("Cooking Guild", 5),
	WOODCUTTING_GUILD("Woodcutting Guild", 5),
	FARMING_GUILD("Farming Guild", 5),
	MISCELLANIA("Miscellania", 6),
	GRAND_EXCHANGE("Grand Exchange", 6),
	FALADOR_PARK("Falador Park", 6),
	DONDAKANS_ROCK("Dondakans' Rock", 6),
	EDGEVILLE("Edgeville", 7),
	KARAMJA("Karamja", 7),
	DRAYNOR_VILLAGE("Draynor Village", 7),
	AL_KHARID("Al Kharid", 7);

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val WIDGET_ID: Int = JEWELLERYBOX_WIDGET
	override val OBJECT_NAMES = arrayOf(BASIC_JEWELLERY_BOX, FANCY_JEWELLERY_BOX, ORNATE_JEWELLERY_BOX)
	override fun toString(): String {
		return "JewelleryBoxTeleport($name)"
	}

	companion object {
		fun forName(name: String): JewelleryBoxTeleport? {
			return if (!name.contains("jewelry box")) null
			else values().firstOrNull { name.contains(it.name.replace("_", " "), true) }
		}
	}

}