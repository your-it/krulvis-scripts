package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.HintArrow
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mole.GiantMole

class LookForMole(script: GiantMole) : Leaf<GiantMole>(script, "Looking for mole") {
	private val centerTile = Tile(1761, 5185, 0)
	override fun execute() {
		Prayer.activePrayers().forEach {
			Prayer.prayer(it, false)
		}

		val mole = script.findMole()
		val hintTile = HintArrow.tile()
		if (mole.valid()) {
			Movement.step(hintTile)
			waitFor { mole.distance() <= 20 }
		} else if (script.respawnTimer.isFinished()) {
			Movement.step(hintTile)
			waitFor { mole.distance() <= 20 }
		} else if (centerTile.distance() > 10) {
			Movement.step(centerTile)
			waitFor { centerTile.distance() < 10 }
		}
	}
}