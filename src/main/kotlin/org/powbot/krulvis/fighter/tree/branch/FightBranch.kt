package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Actor
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.*

class IsKilling(script: Fighter) : Branch<Fighter>(script, "Killing?") {
	override val failedComponent: TreeComponent<Fighter> = ShouldReanimate(script)
	override val successComponent: TreeComponent<Fighter> = ShouldSipPotion(script, WaitWhileKilling(script))

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

class IsWaitingForLoot(script: Fighter) : Branch<Fighter>(script, "Waiting for loot?") {

	override val successComponent: TreeComponent<Fighter> = WaitForLoot(script)
	override val failedComponent: TreeComponent<Fighter> = CanKill(script)

	override fun validate(): Boolean {
		return script.waitForLootAfterKill && script.isLootWatcherActive()
	}
}

class CanKill(script: Fighter) : Branch<Fighter>(script, "Can Kill?") {

	override val successComponent: TreeComponent<Fighter> = ShouldHop(script)
	override val failedComponent: TreeComponent<Fighter> = WalkToSpot(script)

	override fun validate(): Boolean {
		return !script.useSafespot || script.centerTile() == Players.local().tile()
	}
}

class ShouldHop(script: Fighter) : Branch<Fighter>(script, "Should hop?") {

	override val successComponent: TreeComponent<Fighter> = HopFromPlayers(script)
	override val failedComponent: TreeComponent<Fighter> = ShouldSipPotion(script, Attack(script))

	override fun validate(): Boolean {
		return script.hopFromPlayers && Players.stream().within(script.centerTile(), script.radius).notLocalPlayer().count() >= script.playerHopAmount
	}
}