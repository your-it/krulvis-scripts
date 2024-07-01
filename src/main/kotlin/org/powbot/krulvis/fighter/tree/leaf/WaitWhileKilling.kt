package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class WaitWhileKilling(script: Fighter) : Leaf<Fighter>(script, "Wait for kill confirm...") {
	override fun execute() {
		Chat.clickContinue()
		if (script.canActivatePrayer()) {
			Prayer.quickPrayer(true)
		}

		val target = Players.local().interacting()
		val safespot = script.centerTile()
		if (waitFor(600, 1000) { script.shouldReturnToSafespot() || target.died() }) {
			script.logger.info("Npc died=${target.died()}")
			if (script.shouldReturnToSafespot()) {
				Movement.step(safespot, 0)
			}
		}
	}

	private fun Actor<*>.died() = (healthBarVisible() && healthPercent() == 0) || !refresh().valid()
}