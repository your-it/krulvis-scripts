package org.powbot.krulvis.api.teleports

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor

class TeleportMethod(val teleport: Teleport?) {

	var executed = false

	fun execute(): Boolean {
		if (executed || teleport == null) return true
		val tile = me.tile()
		if (teleport.execute())
			executed = waitFor(long()) { tile.distance() > 50 }
		return executed
	}
}