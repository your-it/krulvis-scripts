package org.powbot.krulvis.woodcutter.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.krulvis.woodcutter.tree.leaf.Drop
import org.powbot.krulvis.woodcutter.tree.leaf.HandleBank

class ShouldBank(script: Woodcutter) : Branch<Woodcutter>(script, "Should Bank?") {
    override val failedComponent: TreeComponent<Woodcutter> = AtSpot(script)
    override val successComponent: TreeComponent<Woodcutter> = ShouldDrop(script)

    override fun validate(): Boolean {
        return Inventory.isFull()
    }
}

class ShouldDrop(script: Woodcutter) : Branch<Woodcutter>(script, "Should Drop?") {
    override val failedComponent: TreeComponent<Woodcutter> = IsBankOpen(script)
    override val successComponent: TreeComponent<Woodcutter> = Drop(script)

    override fun validate(): Boolean {
        return !script.bank
    }
}

class IsBankOpen(script: Woodcutter) : Branch<Woodcutter>(script, "Is Bank Open?") {
    override val failedComponent: TreeComponent<Woodcutter> = SimpleLeaf(script, "Open bank") { Bank.openNearestBank() }
    override val successComponent: TreeComponent<Woodcutter> = HandleBank(script)

    override fun validate(): Boolean {
        return Bank.opened()
    }
}