package org.powbot.krulvis.thiever.tree.branch

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever
import org.powbot.krulvis.thiever.tree.leaf.Eat
import org.powbot.krulvis.thiever.tree.leaf.HandleBank
import org.powbot.krulvis.thiever.tree.leaf.OpenPouch
import org.powbot.krulvis.thiever.tree.leaf.Pickpocket

class ShouldEat(script: Thiever) : Branch<Thiever>(script, "Should Eat") {
    override val successComponent: TreeComponent<Thiever> = Eat(script)
    override val failedComponent: TreeComponent<Thiever> = ShouldOpenCoinPouch(script, ShouldBank(script), 28)

    override fun validate(): Boolean {
        val food = script.food
        return food.inInventory() && (food.canEat() || currentHP() <= 4)
    }
}

class ShouldBank(script: Thiever) : Branch<Thiever>(script, "Should Bank") {
    override val successComponent: TreeComponent<Thiever> = ShouldOpenCoinPouch(script, IsBankOpen(script), 1)
    override val failedComponent: TreeComponent<Thiever> = AtSpot(script)

    override fun validate(): Boolean {
        return Inventory.isFull() || (currentHP() < 8 && !script.food.inInventory())
    }
}

class ShouldOpenCoinPouch(script: Thiever, nextNode: TreeComponent<Thiever>, private val stackSize: Int) :
    Branch<Thiever>(script, "Should Bank") {
    override val successComponent: TreeComponent<Thiever> = OpenPouch(script)
    override val failedComponent: TreeComponent<Thiever> = nextNode

    override fun validate(): Boolean {
        return (Inventory.stream().name("Coin pouch").firstOrNull()?.stack ?: 0) >= stackSize
    }
}

class IsBankOpen(script: Thiever) : Branch<Thiever>(script, "Should Open Bank") {
    override val successComponent: TreeComponent<Thiever> = HandleBank(script)
    override val failedComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Open bank") {

        Bank.openNearestBank()
    }

    override fun validate(): Boolean {
        return Bank.opened()
    }
}

class AtSpot(script: Thiever) : Branch<Thiever>(script, "AtSpot?") {
    override val successComponent: TreeComponent<Thiever> = Pickpocket(script)
    override val failedComponent: TreeComponent<Thiever> = SimpleLeaf(script, "Walking") {
        Bank.close()
        if (Players.local().tile().floor > 0) {
            script.log.info("Climbing down first...")
            if (interact(Objects.stream().action("Climb-down").nearest().firstOrNull(), "Climb-down")) {
                waitFor(long()) { validate() }
            }
        } else {
            Movement.walkTo(script.lastTile)
        }
    }

    override fun validate(): Boolean {
        return (script.getTarget()?.distance() ?: 99) < 20
    }
}