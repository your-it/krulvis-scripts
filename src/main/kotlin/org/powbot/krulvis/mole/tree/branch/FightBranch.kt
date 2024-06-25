package org.powbot.krulvis.mole.tree.branch

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.mole.GiantMole
import org.powbot.krulvis.mole.tree.leaf.FindMole
import org.powbot.krulvis.mole.tree.leaf.FlickOffensive
import org.powbot.krulvis.mole.tree.leaf.GoToMole

val moleArea = Area(Tile(1700, 5130, 0), Tile(1800, 5250, 0))

class AtMole(script: GiantMole) : Branch<GiantMole>(script, "Should Sip Potion?") {

	override val failedComponent: TreeComponent<GiantMole> = GoToMole(script)
	override val successComponent: TreeComponent<GiantMole> = NearMole(script)

	override fun validate(): Boolean {
		return moleArea.contains(me)
	}
}

class NearMole(script: GiantMole) : Branch<GiantMole>(script, "Is near mole?") {

	override val failedComponent: TreeComponent<GiantMole> = FindMole(script)
	override val successComponent: TreeComponent<GiantMole> = ShouldSipPotion(script, ProtectMelee(script))

	override fun validate(): Boolean {
		return script.findMole().distance() < 20
	}
}

class ProtectMelee(script: GiantMole) : Branch<GiantMole>(script, "Is Protect on?") {

	override val failedComponent: TreeComponent<GiantMole> = SimpleLeaf(script, "Put Protect On") {
		if (Prayer.prayer(Prayer.Effect.PROTECT_FROM_MELEE, true)) {
			waitFor(1000) { Prayer.prayerActive(Prayer.Effect.PROTECT_FROM_MELEE) }
		}
	}
	override val successComponent: TreeComponent<GiantMole> = IsFighting(script)

	override fun validate(): Boolean {
		return Prayer.activePrayers().contains(Prayer.Effect.PROTECT_FROM_MELEE)
	}
}

class IsFighting(script: GiantMole) : Branch<GiantMole>(script, "Is Fighting?") {

	override val failedComponent: TreeComponent<GiantMole> = SimpleLeaf(script, "Attack Mole") {
		val mole = script.findMole()
		if (walkAndInteract(mole, "Attack")) {
			waitForDistance(mole) { me.interacting() == mole }
		}
	}
	override val successComponent: TreeComponent<GiantMole> = FlickOffensive(script)

	override fun validate(): Boolean {
		return me.interacting() == script.findMole()
	}
}