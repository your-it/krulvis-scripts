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

class WalkToBoat(script: Tempoross) : Leaf<Tempoross>(script, "Walk to boat") {
	override fun execute() {
		//Reset the side for the next run...
		script.side = Side.UNKNOWN

		if (script.lastGame) {
			ScriptManager.stop()
			return
		}

		if (!waitFor(6000) { script.energy > -1 || script.getLadder().valid() }) {
			debug("Walking with web first")
			Movement.walkTo(Tile(3137, 2841, 0))
		} else {
			debug("We don't actually have to walk...")
		}

	}
}
