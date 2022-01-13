package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.extensions.items.Item.Companion.DARK_KEY
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Utils

class ShouldSipPotion<S : ATScript>(script: S, override val failedComponent: TreeComponent<S>) :
    Branch<S>(script, "Should sip potion?") {

    override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Sipping") {
        if (potion!!.drink()) {
            Condition.wait({ potion() == null }, 250, 15)
            nextRestore = Random.nextInt(45, 60)
        }
    }

    var potion: Potion? = null
    var nextRestore = Random.nextInt(45, 60)

    fun potion(): Potion? = Potion.values().filter { it.hasWith() }
        .firstOrNull {
            val restore = if (it == Potion.PRAYER) 100 + nextRestore else nextRestore
            it.needsRestore(restore)
        }

    override fun validate(): Boolean {
        potion = potion()
        return potion != null
    }
}