package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.mobile.script.ScriptManager

class EnterBoat(script: Tempoross) : Leaf<Tempoross>(script, "Entering boat") {
	override fun execute() {
		//Reset the side for the next run...
		script.side = Side.UNKNOWN

		if (script.lastGame) {
			ScriptManager.stop()
			return
		}
		val ropeLadder = script.getLadder()
		ropeLadder.bounds(41, 72, -384, -140, -52, 42)
		if (!waitFor(2000) { Game.clientState() == 30 }) {
			debug("Game not loaded, waiting before walking")
			return
		}
		if (ropeLadder.distance() > 10 || !ropeLadder.inViewport()) {
			debug("Walking with Movement.step towards ladder")
			Movement.step(ropeLadder)
		} else if (ropeLadder.interact(if (script.solo) "Solo-start" else "Quick-climb")) {
			waitFor(long()) { BOAT_AREA.contains(me.tile()) || script.energy > -1 }
		}
	}
}
