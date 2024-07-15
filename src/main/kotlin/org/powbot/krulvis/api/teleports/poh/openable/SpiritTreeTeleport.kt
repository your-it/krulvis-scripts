package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val FARMING_GUILD_SPIRIT_TREE_POH = "Farming Guild spirit tree (POH)"
enum class SpiritTreeTeleport(override val action: String) : OpenableHouseTeleport {
	FarmingGuild("Farming guild"),
	;

	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val requirements: List<Requirement> = emptyList()
	override val OBJECT_NAMES = arrayOf("Digsite Pendant")
	override val WIDGET_ID: Int = -1
	override val COMP_ID: Int = -1
	override fun toString(): String {
		return "SpiritTreeTeleport($name)"
	}

	companion object {
		fun forName(name: String): SpiritTreeTeleport? {
			return if (!name.contains("spirit tree")) null
			else values().firstOrNull { name.replace(" ", "").contains(it.name, true) }
		}
	}
}