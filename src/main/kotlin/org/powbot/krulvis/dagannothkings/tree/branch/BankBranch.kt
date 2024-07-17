package org.powbot.krulvis.dagannothkings.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.dagannothkings.DagannothKings

class ShouldBank(script: DagannothKings): Branch<DagannothKings>(script, "ShouldBank?"){
	override val failedComponent: TreeComponent<DagannothKings> = AtKings(script)
	override val successComponent: TreeComponent<DagannothKings> = IsBankOpen(script)

	override fun validate(): Boolean {
		return false
	}
}

class IsBankOpen(script: DagannothKings): Branch<DagannothKings>(script, "IsBankOpen?"){
	override val failedComponent: TreeComponent<DagannothKings>
		get() = TODO("Not yet implemented")
	override val successComponent: TreeComponent<DagannothKings>
		get() = TODO("Not yet implemented")

	override fun validate(): Boolean {
		return Bank.opened()
	}
}