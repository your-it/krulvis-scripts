package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings

class Attack(script: DagannothKings) : Leaf<DagannothKings>(script, "Attack") {

	val attackTimer = Timer(1800)
	override fun execute() {
		val target = script.getNewTarget()
		target.bounds(-52, 52, -252, 0, -32, 32)
		if (!target.valid() || !attackTimer.isFinished() || target.distance() > 14) return

		val otherCloser = Npcs.stream().nameContains("Dagannoth").filtered { it.name != target.name && !it.dead() }.any { it.distance() < target.distance() }
		if (otherCloser) {
			script.logger.info("Not attacking because there's another closer")
		} else if (target.interact("Attack")) {
			attackTimer.reset()
			waitFor { me.interacting() == target }
		}
	}
}