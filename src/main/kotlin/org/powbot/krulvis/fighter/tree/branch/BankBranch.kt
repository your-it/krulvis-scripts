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
    override val failedComponent: TreeComponent<Fighter> = CanLoot(script)

    override fun validate(): Boolean {
        val hasFood = Inventory.containsOneOf(*script.food.ids)
        return !hasFood && (script.needFood() || Inventory.isFull())
    }
}

class IsBankOpen(
    script: Fighter,
    override val successComponent: TreeComponent<Fighter> = HandleBank(script),
    override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Open Bank") { script.bank.open() },
) : Branch<Fighter>(script, "Should Bank") {

    override fun validate(): Boolean {
        return Bank.opened()
    }
}