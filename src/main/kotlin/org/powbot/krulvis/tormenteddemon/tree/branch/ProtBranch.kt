package org.powbot.krulvis.tormenteddemon.tree.branch

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.tormenteddemon.TormentedDemon

class ShouldSwitchProtPray(script: TormentedDemon) : Branch<TormentedDemon>(script, "ShouldSwitchProtPray?") {
	override val failedComponent: TreeComponent<TormentedDemon> = CanLoot(script)
	override val successComponent: TreeComponent<TormentedDemon> = SimpleLeaf(script, "SwitchPray") {
		Prayer.prayer(script.protectionPrayer, true)
	}

	override fun validate(): Boolean {
		if (!script.currentTarget.valid()) {
			return false
		}
		return script.currentTarget.valid() && !Prayer.prayerActive(script.protectionPrayer) && Prayer.prayerPoints() > 0
	}
}