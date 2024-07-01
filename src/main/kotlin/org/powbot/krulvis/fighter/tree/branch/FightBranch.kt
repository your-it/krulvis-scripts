package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Attack
import org.powbot.krulvis.fighter.tree.leaf.WalkToSpot

class Killing(script: Fighter) : Branch<Fighter>(script, "Killing?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldReanimate(script)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Killing..") {
		Chat.clickContinue()
		if (script.hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0) {
			Prayer.quickPrayer(true)
		}

		val safespot = script.centerTile()
		if (Condition.wait { script.shouldReturnToSafespot() }) {
			Movement.step(safespot, 0)
		}
	}


	override fun validate(): Boolean {
		return killing()
	}

	companion object {
		fun killing(): Boolean {
			val interacting = Players.local().interacting()
			if (interacting == Actor.Nil) return false
			return interacting.healthBarVisible() && TargetWidget.health() > 0
		}
	}
}

class CanKill(script: Fighter) : Branch<Fighter>(script, "Can Kill?") {

	override val successComponent: TreeComponent<Fighter> = Attack(script)
	override val failedComponent: TreeComponent<Fighter> = WalkToSpot(script)

	override fun validate(): Boolean {
		if (script.useSafespot && script.centerTile() != Players.local().tile()) {
			return false
		}
		return !script.waitForLootAfterKill || !script.isWaitingForLoot()
	}
}