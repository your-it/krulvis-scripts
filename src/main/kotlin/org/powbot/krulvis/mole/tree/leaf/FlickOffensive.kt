package org.powbot.krulvis.mole.tree.leaf

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mole.GiantMole
import kotlin.random.Random

class FlickOffensive(script: GiantMole) : Leaf<GiantMole>(script, "FlickOffensive") {
	override fun execute() {
		val splats = script.moleSplatCycles
		if (splats.isEmpty()) return
		val lastAttack = Game.cycle() - splats[0]
		val shouldAttack = lastAttack > 100
		if (shouldAttack) {
			Prayer.prayer(Prayer.Effect.PIETY, true)
			waitFor {
				val newSplats = script.moleSplatCycles
				newSplats.isNotEmpty() && Game.cycle() - newSplats[0] < 100
			}
		}
		Prayer.prayer(Prayer.Effect.PIETY, false)
	}
}