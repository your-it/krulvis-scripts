package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.lizardshamans.LizardShamans

class OpenBank(script: LizardShamans) : Leaf<LizardShamans>(script, "Open Bank") {
	override fun execute() {
		Bank.open()
	}
}