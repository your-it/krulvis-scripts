package org.powbot.krulvis.lizardshamans.tree.branch

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.lizardshamans.Data.SHAMAN_AREAS
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.krulvis.lizardshamans.tree.leaf.GoToShamans
import org.powbot.krulvis.lizardshamans.tree.leaf.Loot

class AtShamans(script: LizardShamans) : Branch<LizardShamans>(script, "AtShamans?") {
	override val failedComponent: TreeComponent<LizardShamans> = GoToShamans(script)
	override val successComponent: TreeComponent<LizardShamans> = IsPrayOn(script)

	override fun validate(): Boolean {
		return SHAMAN_AREAS.any { it.contains(me) }
	}
}

class IsPrayOn(script: LizardShamans) : Branch<LizardShamans>(script, "IsPrayOn?") {
	override val failedComponent: TreeComponent<LizardShamans> = SimpleLeaf(script, "TurnPrayOn") {
		if (Prayer.quickPrayer(true)) {
			Utils.waitFor { Prayer.quickPrayer() }
		}
	}
	override val successComponent: TreeComponent<LizardShamans> = ShouldLoot(script)

	override fun validate(): Boolean {
		return Prayer.quickPrayer()
	}
}

class ShouldLoot(script: LizardShamans) : Branch<LizardShamans>(script, "ShouldLoot?") {
	override val failedComponent: TreeComponent<LizardShamans> = ShouldRunToSafety(script)
	override val successComponent: TreeComponent<LizardShamans> = Loot(script)

	override fun validate(): Boolean {
		return script.lootList.isNotEmpty()
	}
}