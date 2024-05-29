package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement

private const val JEWELLERYBOX_WIDGET = 590
private const val JEWELLERYBOX_COMPONENT = 1
private const val JEWELLERYBOX_CLOSE_BUTTON = 11

private const val BASIC_JEWELLERY_BOX = "Basic jewellery box"
private const val FANCY_JEWELLERY_BOX = "Fancy jewellery box"
private const val ORNATE_JEWELLERY_BOX = "Ornate jewellery box"

const val PVP_ARENA_JEWELLERY_BOX = "PvP arena jewelry box (POH)"

enum class JewelleryBoxTeleport(override val action: String, override val COMP_ID: Int) : OpenableHouseTeleport {
	PvPArena("PvP Arena", 2),
	CastleWars("Castle Wars", 2),
	FeroxEnclave("Ferox Enclave", 2),
	Burthorpe("Burthorpe", 3),
	BarbarianOutpost("Barbarian Outpost", 3),
	CorporealBeast("Corporeal Beast", 3),
	TearsOfGuthix("Tears of Guthix", 3),
	Wintertodt("Wintertodt Camp", 3),
	WarriorsGuild("Warriors' Guild", 4),
	ChampionsGuild("Champions' Guild", 4),
	Monastery("Monastery", 3),
	RangingGuild("Ranging Guild", 4),
	FishingGuild("Fishing Guild", 5),
	MiningGuild("Mining Guild", 5),
	CraftingGuild("Crafting Guild", 5),
	CooksGuild("Cooking Guild", 5),
	WoodcuttingGuild("Woodcutting Guild", 5),
	FarmingGuild("Farming Guild", 5),
	Miscellania("Miscellania", 6),
	GrandExchange("Grand Exchange", 6),
	FaladorPark("Falador Park", 6),
	Dondakan("Dondakans' Rock", 6),
	Edgeville("Edgeville", 7),
	Karamja("Karamja", 7),
	DraynorVillage("Draynor Village", 7),
	AlKharid("Al Kharid", 7);

	override val requirements: List<Requirement> = emptyList()
	override val WIDGET_ID: Int = JEWELLERYBOX_WIDGET
	override val OBJECT_NAMES = arrayOf(BASIC_JEWELLERY_BOX, FANCY_JEWELLERY_BOX, ORNATE_JEWELLERY_BOX)

	companion object {
		fun forName(name: String) = values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
	}

}