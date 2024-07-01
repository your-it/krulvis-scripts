package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.branch.IsKilling

class Attack(script: Fighter) : Leaf<Fighter>(script, "Attacking") {
	override fun execute() {
		val target = script.target()
		if (script.canActivatePrayer()) {
			Prayer.quickPrayer(true)
		}
		if (attack(target)) {
			script.currentTarget = target
			Condition.wait({
				IsKilling.killing() || script.shouldReturnToSafespot()
			}, 250, 10)
			if (script.shouldReturnToSafespot()) {
				Movement.step(script.centerTile(), 0)
			}
		}
	}

	fun attack(target: Npc?): Boolean {
		return if (script.useSafespot) {
			target?.interact("Attack") == true
		} else {
			ATContext.walkAndInteract(target, "Attack")
		}
	}
}