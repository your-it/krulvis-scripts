package org.powbot.krulvis.woodcutter.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.krulvis.woodcutter.tree.leaf.Drop
import org.powbot.krulvis.woodcutter.tree.leaf.HandleBank

class ShouldBank(script: Woodcutter) : Branch<Woodcutter>(script, "Should Bank?") {
    override val failedComponent: TreeComponent<Woodcutter> = ShouldPickNest(script)
    override val successComponent: TreeComponent<Woodcutter> = ShouldDrop(script)

    override fun validate(): Boolean {
        return Inventory.isFull()
    }
}

class ShouldPickNest(script: Woodcutter) : Branch<Woodcutter>(script, "Should Pick nest?") {
    override val failedComponent: TreeComponent<Woodcutter> = AtSpot(script)
    override val successComponent: TreeComponent<Woodcutter> = SimpleLeaf(script, "Pick nest") {
        val tile = nest!!.tile
        if (Utils.walkAndInteract(nest, "Take")) {
            waitFor(long()) { GroundItems.stream().at(tile).none { it.name() == "Bird nest" } }
        }
    }

    var nest: GroundItem? = null

    override fun validate(): Boolean {
        if (!script.bank) return false
        nest = script.nest()
        return nest != null
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