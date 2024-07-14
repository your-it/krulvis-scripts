package org.powbot.krulvis.lizardshamans.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.lizardshamans.LizardShamans
import org.powbot.krulvis.lizardshamans.tree.leaf.HandleBank
import org.powbot.krulvis.lizardshamans.tree.leaf.OpenBank

class ShouldBank(script: LizardShamans) : Branch<LizardShamans>(script, "ShouldBank?") {
	override val failedComponent: TreeComponent<LizardShamans> = AtShamans(script)
	override val successComponent: TreeComponent<LizardShamans> = IsBankOpen(script)

	override fun validate(): Boolean {
		return false
	}
}

class IsBankOpen(script: LizardShamans) : Branch<LizardShamans>(script, "ShouldOpenBank?") {
	override val failedComponent: TreeComponent<LizardShamans> = OpenBank(script)
	override val successComponent: TreeComponent<LizardShamans> = HandleBank(script)

	override fun validate(): Boolean {
		return Bank.opened()
	}
}