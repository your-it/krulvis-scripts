package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.rt4.HintArrow
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mole.GiantMole

class FindMole(script: GiantMole) : Leaf<GiantMole>(script, "Finding mole") {
	override fun execute() {
		Prayer.activePrayers().forEach {
			Prayer.prayer(it, false)
		}

		val hintTile = HintArrow.tile()
		Movement.step(hintTile)
		waitFor { script.findMole().distance() <= 20 }
	}
}