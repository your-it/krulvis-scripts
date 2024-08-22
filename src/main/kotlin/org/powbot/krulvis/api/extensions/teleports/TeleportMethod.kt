package org.powbot.krulvis.api.extensions.teleports

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor

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