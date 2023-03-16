package org.powbot.krulvis.barrows.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.barrows.Barrows

class ShouldBank(script: Barrows) : Branch<Barrows>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<Barrows>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Barrows> = OpenedBank(script)

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }
}

class OpenedBank(script: Barrows) : Branch<Barrows>(script, "ShouldBank?") {
    override val failedComponent: TreeComponent<Barrows>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Barrows>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        return Bank.opened()
    }
}