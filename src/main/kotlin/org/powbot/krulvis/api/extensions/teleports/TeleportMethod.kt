package org.powbot.krulvis.api.extensions.teleports

import org.powbot.api.Tile
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Movement
import org.powbot.krulvis.api.ATContext.logger
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor

class TeleportMethod(val teleport: Teleport?) {

	var executed = false

	fun execute(): Boolean {
		logger.info("Performing teleport=$teleport")
		if (executed || teleport == null) return true
		val tile = me.tile()
		if (Combat.wildernessLevel() > teleport.wildernessLevel) {
			logger.info("In too high wilderness! Walking south before teleporting")
			Movement.step(Tile(tile.x, tile.y - 15, tile.floor))
			waitFor { Combat.wildernessLevel() <= teleport.wildernessLevel }
		} else if (teleport.execute())
			executed = waitFor(long()) { tile.distance() > 50 }
		return executed
	}
}