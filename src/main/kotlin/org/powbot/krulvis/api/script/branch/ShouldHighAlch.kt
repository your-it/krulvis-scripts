package org.powbot.krulvis.api.script.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.extensions.items.Item.Companion.DARK_KEY
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Utils

class ShouldHighAlch<S : ATScript>(script: S, override val failedComponent: TreeComponent<S>) :
    Branch<S>(script, "Should high alch?") {
    override val successComponent: TreeComponent<S> = SimpleLeaf(script, "High alching") {
        if (!Magic.casting(spell)) {
            if (spell.cast()) {
                Utils.waitFor { Magic.casting(spell) }
            }
        }
        if (Magic.casting(spell)) {
            val count = Inventory.stream().id(alchable!!.id).count()
            alchable?.interact("Cast")
            Utils.waitFor { Inventory.stream().id(alchable!!.id).count() != count }
        }
    }


    val spell = Magic.Spell.HIGH_ALCHEMY
    var alchable: Item? = null

    fun alchable(): Item? {
        val lootIds = script.painter.paintBuilder.items
            .filter { row -> row.any { it is InventoryItemPaintItem } }
            .map { row -> (row.first { it is InventoryItemPaintItem } as InventoryItemPaintItem).itemId }
            .toIntArray()
        return Inventory.stream().id(*lootIds).firstOrNull {
            val value = it.value()
            it.id !in skip && value > 250 && !it.stackable() && value / it.getPrice().toDouble() > .9
        }
    }

    val skip = intArrayOf(DARK_KEY)

    override fun validate(): Boolean {
        alchable = alchable()
        return alchable != null && spell.canCast()
    }
}