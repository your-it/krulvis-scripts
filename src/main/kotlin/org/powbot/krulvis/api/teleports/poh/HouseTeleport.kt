package org.powbot.krulvis.api.teleports.poh

import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.teleports.SpellTeleport
import org.powbot.krulvis.api.teleports.Teleport
import org.powbot.krulvis.api.utils.Utils.waitFor

interface HouseTeleport : Teleport {
	override fun execute(): Boolean {
		if (!House.isInside()) {
			if (toHouseTeleport.execute()) {
				waitFor(5000) { House.isInside() }
			}
		} else if (House.useRestorePool()) {
			return insideHouseTeleport()
		}
		return false
	}

	fun insideHouseTeleport(): Boolean

	companion object {
		var toHouseTeleport: Teleport = SpellTeleport.HOUSE_TELEPORT
	}

}

