package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Constants.GAME_LOADED
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
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
		if (!waitFor(2000) { Game.clientState() == GAME_LOADED }) {
			debug("Game not loaded, waiting before walking")
			return
		}
		if (!ropeLadder.valid()) {
			debug("Can't find rope ladder... waiting to see if we're in game")
			if (!waitFor(6000) { script.energy > -1 }) {
				debug("Walking with web first")
				Movement.walkTo(Tile(3137, 2841, 0))
			}
		} else if (ropeLadder.distance() > 10 || !ropeLadder.inViewport()) {
			debug("Walking with Movement.step towards ladder")
			Movement.step(ropeLadder)
		} else if (ropeLadder.interact("Quick-climb")) {
			waitFor(long()) { BOAT_AREA.contains(ATContext.me.tile()) }
		}
	}
}
