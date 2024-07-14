package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.BoundingModel
import org.powbot.api.Input
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.lizardshamans.Data
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.krulvis.lizardshamans.dead

class AttackShaman(script: LizardShamans) : Leaf<LizardShamans>(script, "AttackShaman") {
	override fun execute() {
		if (!script.jumpEvent.timer.isFinished()) {
			script.logger.info("Not attacking because jumpEvent not finished ${script.jumpEvent}")
			return
		}
		val target = if (script.target.valid() && !script.target.dead()) script.target else getTarget()
		if (target.animation() == Data.AttackAnimation.Falling.animation || target.animation() == Data.AttackAnimation.Jump.animation) {
			script.logger.info("Not attacking in middle of jump animation")
			return
		}

		target.bounds(-42, 12, -282, -120, -52, 62)
		if (target.interact("Attack")) {
			waitFor { me.interacting() == target }
		}
	}

	fun getTarget(): Npc {
		val shamans = Npcs.stream().name("Lizardman shaman").nearest().filtered {
			!it.dead() && it.reachable()
		}.toList()
		return shamans.firstOrNull { it.interacting() == me } ?: shamans.firstOrNull() ?: Npc.Nil
	}
}