package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.teleports.poh.openable.OpenableHouseTeleport

interface Teleport {

	val action: String
	val requirements: List<Requirement>
	fun execute(): Boolean

	companion object {

		fun forName(name: String): Teleport? {
			return SpellTeleport.forName(name) ?: ItemTeleport.forName(name) ?: OpenableHouseTeleport.forName(name)
		}
	}
}