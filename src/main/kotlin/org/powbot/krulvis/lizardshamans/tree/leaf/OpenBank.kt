package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.lizardshamans.LizardShamans

class OpenBank(script: LizardShamans) : Leaf<LizardShamans>(script, "Open Bank") {
	override fun execute() {
		script.banking = true
		val bank = Bank.getBank()
		if (!bank.valid()) {
			if (script.bankTeleport.execute()) {
				Movement.walkTo(bank)
			}
		} else if (walkAndInteract(bank, "Open")) {
			waitForDistance(bank) { Bank.opened() }
		}
	}
}