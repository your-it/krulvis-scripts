package org.powbot.krulvis.mixology.tree.branches

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.mixology.Data.LAB_AREA
import org.powbot.krulvis.mixology.Mixology

class AtMixologyLab(script: Mixology) : Branch<Mixology>(script, "AtLab") {
	override val failedComponent: TreeComponent<Mixology> = SimpleLeaf(script, "WalkToLab") {
		Movement.walkTo(LAB_AREA.centralTile)
	}
	override val successComponent: TreeComponent<Mixology> = HasFinishedPotion(script)

	override fun validate(): Boolean {
		return LAB_AREA.contains(me)
	}
}