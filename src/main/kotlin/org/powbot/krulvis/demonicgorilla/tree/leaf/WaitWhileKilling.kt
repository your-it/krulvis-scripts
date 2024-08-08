package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class WaitWhileKilling(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Wait for kill confirm...") {
	override fun execute() {

		val offensivePrayer = script.offensivePrayer
		script.logger.info("defensivePray=${script.protectionPrayer}, offensivePray=${script.offensivePrayer}")
		if (!Prayer.prayerActive(script.protectionPrayer)) {
			Prayer.prayer(script.protectionPrayer, true)
		}
		if (offensivePrayer != null && !Prayer.prayerActive(offensivePrayer)) {
			Prayer.prayer(offensivePrayer, true)
		}

		if (script.equipment != script.meleeEquipment && script.currentTarget.distance() <= 1) {
			script.logger.info("Walking step back because ranging")
//			Movement.step()
		}

	}
}