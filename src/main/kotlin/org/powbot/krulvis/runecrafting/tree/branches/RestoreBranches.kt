package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.fullPrayer
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.ouraniaPathToAltar
import org.powbot.krulvis.runecrafting.tree.leafs.CastVileVigour

class ShouldPrayAtAltar(script: Runecrafter) : Branch<Runecrafter>(script, "Should pray at Chaos Altar") {
	override val failedComponent: TreeComponent<Runecrafter> = ShouldCastVileVigour(script)
	override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Pray at altar") {
		val altar = script.findChaosAltar()
		if (altar.distance() > 4 || !altar.inViewport()) {
			ouraniaPathToAltar.traverse(1, distanceToLastTile = 4)
		} else if (walkAndInteract(altar, "Pray-at")) {
			waitForDistance(altar) { fullPrayer() }
		}
	}


	override fun validate(): Boolean {
		if (fullPrayer()) return false
		script.chaosAltar = script.findChaosAltar()
		return script.chaosAltar.valid()
	}
}


class ShouldCastVileVigour(script: Runecrafter) : Branch<Runecrafter>(script, "Should cast Vile Vigour") {
	override val failedComponent: TreeComponent<Runecrafter> = ShouldBank(script)
	override val successComponent: TreeComponent<Runecrafter> = CastVileVigour(script)

	override fun validate(): Boolean {
		if (Movement.energyLevel() > 50) return false
		return script.vileVigour && fullPrayer() && script.findChaosAltar().valid() && Magic.ArceuusSpell.VILE_VIGOUR.canCast()
	}
}