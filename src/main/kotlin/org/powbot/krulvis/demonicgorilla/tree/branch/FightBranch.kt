package org.powbot.krulvis.demonicgorilla.tree.branch

import org.powbot.api.rt4.Actor
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.demonicgorilla.Data
import org.powbot.krulvis.demonicgorilla.DemonicGorilla
import org.powbot.krulvis.demonicgorilla.tree.leaf.Attack
import org.powbot.krulvis.demonicgorilla.tree.leaf.WaitWhileKilling
import org.powbot.krulvis.demonicgorilla.tree.leaf.WalkToSpot

class ShouldDodgeProjectile(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "ShouldDodgeProjectile?") {
	override val failedComponent: TreeComponent<DemonicGorilla> = ShouldSwitchProtPray(script)
	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "Dodge Rock") {
		Movement.step(script.projectileSafespot, 0)
		sleep(600)
		script.currentTarget.interact("Attack")
	}

	override fun validate(): Boolean {
		val dest = Movement.destination()
		val tile = if (dest.valid()) dest else Players.local().tile()
		return script.projectiles.any { it.first.destination() == tile }
	}
}

class ShouldSwitchProtPray(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "ShouldSwitchProtPray?") {
	override val failedComponent: TreeComponent<DemonicGorilla> = IsKilling(script)
	override val successComponent: TreeComponent<DemonicGorilla> = SimpleLeaf(script, "SwitchPray") {
		Prayer.prayer(script.protectionPrayer, true)
	}

	override fun validate(): Boolean {
		if (!script.currentTarget.valid()) {
			return false
		}
		return script.currentTarget.valid() && !Prayer.prayerActive(script.protectionPrayer) && Prayer.prayerPoints() > 0
	}
}

class IsKilling(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Killing?") {
	override val failedComponent: TreeComponent<DemonicGorilla> = CanKill(script)
	override val successComponent: TreeComponent<DemonicGorilla> = ShouldConsume(script, WaitWhileKilling(script))

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


class CanKill(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "Can Kill?") {

	override val successComponent: TreeComponent<DemonicGorilla> = Attack(script)
	override val failedComponent: TreeComponent<DemonicGorilla> = WalkToSpot(script)

	override fun validate(): Boolean {
		return script.centerTile.distance() <= 25
	}
}
