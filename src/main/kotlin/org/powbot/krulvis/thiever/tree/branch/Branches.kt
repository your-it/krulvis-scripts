package org.powbot.krulvis.thiever.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.thiever.Thiever
import org.powbot.krulvis.thiever.tree.leaf.Eat
import org.powbot.krulvis.thiever.tree.leaf.HandleBank
import org.powbot.krulvis.thiever.tree.leaf.OpenPouch
import org.powbot.krulvis.thiever.tree.leaf.Pickpocket

class ShouldEat(script: Thiever) : Branch<Thiever>(script, "Should Eat") {
    override val successComponent: TreeComponent<Thiever> = Eat(script)
    override val failedComponent: TreeComponent<Thiever> = ShouldBank(script)

    override fun validate(): Boolean {
        val food = script.food
        return food.inInventory() && food.canEat()
    }
}

class ShouldBank(script: Thiever) : Branch<Thiever>(script, "Should Bank") {
    override val successComponent: TreeComponent<Thiever> = ShouldOpenCoinPouch(script)
    override val failedComponent: TreeComponent<Thiever> = Pickpocket(script)

    override fun validate(): Boolean {
        return Inventory.isFull() || (currentHP() < 8 && !script.food.inInventory())
    }
}

class ShouldOpenCoinPouch(script: Thiever) : Branch<Thiever>(script, "Should Bank") {
    override val successComponent: TreeComponent<Thiever> = OpenPouch(script)
    override val failedComponent: TreeComponent<Thiever> = BankOpen(script)

    override fun validate(): Boolean {
        return Inventory.stream().name("Coin pouch").isNotEmpty()
    }
}

class BankOpen(script: Thiever) : Branch<Thiever>(script, "Should Open Bank") {
    override val successComponent: TreeComponent<Thiever> = HandleBank(script)
    override val failedComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Open bank") {
        Bank.openNearestBank()
    }

    override fun validate(): Boolean {
        return Bank.opened()
    }
}