package org.powbot.krulvis.demonicgorilla.tree.branch

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class ShouldSwitchProtPray(script: DemonicGorilla) : Branch<DemonicGorilla>(script, "ShouldSwitchProtPray?") {
	override val failedComponent: TreeComponent<DemonicGorilla> = CanLoot(script)
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