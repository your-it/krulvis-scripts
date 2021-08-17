package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.HandleBank

class ShouldBank(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Fighter>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        return !Inventory.containsOneOf(*script.food.ids)
    }
}

class IsBankOpen(
    script: Fighter,
    override val successComponent: TreeComponent<Fighter> = HandleBank(script),
    override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Open Bank") { Bank.openNearestBank() },
) : Branch<Fighter>(script, "Should Bank") {

    override fun validate(): Boolean {
        return Bank.opened()
    }
}