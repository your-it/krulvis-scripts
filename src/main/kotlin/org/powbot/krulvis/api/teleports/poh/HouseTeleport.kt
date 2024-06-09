package org.powbot.krulvis.api.teleports.poh

import org.powbot.api.rt4.Movement
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.teleports.SpellTeleport
import org.powbot.krulvis.api.teleports.Teleport
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.waitFor

interface HouseTeleport : Teleport {
	override fun execute(): Boolean {
		logger.info("Executing ${toString()}")
		if (!House.isInside()) {
			if (toHouseTeleport.execute()) {
				waitFor(5000) { House.isInside() }
			}
		} else if (useRestorePool()) {
			return insideHouseTeleport()
		}
		return false
	}

	fun shouldRestorePool(): Boolean {
		return Movement.energyLevel() < 95
	}

	fun useRestorePool(): Boolean {
		if (!shouldRestorePool()) {
			return true
		}

		val pool = House.getPool()

		if (!pool.valid()) {
			return true
		}

		return ATContext.walkAndInteract(pool, "Drink") && Utils.waitForDistance(pool) { Movement.energyLevel() >= 99 }
	}

	fun insideHouseTeleport(): Boolean

	companion object {
		var toHouseTeleport: Teleport = SpellTeleport.HOUSE_TELEPORT
	}

}

