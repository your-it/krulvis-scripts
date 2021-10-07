package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Loot(script: Fighter) : Leaf<Fighter>(script, "Looting") {
    override fun execute() {
        if (script.hasPrayPots && Prayer.quickPrayer()) {
            Prayer.quickPrayer(false)
        }
        val loots = script.loot().sortedWith(compareBy<GroundItem> { it.distance() }
            .thenByDescending { GrandExchange.getItemPrice(it.id()) * it.stackSize() })

        loots.forEachIndexed { i, gi ->
            val id = gi.id()
            //Make space
            if ((!gi.stackable() || !Inventory.containsOneOf(id)) && Inventory.isFull()) {
                val vial = Inventory.stream().id(VIAL).firstOrNull()
                if (vial != null)
                    vial.interact("Drop")
                else if (script.food?.inInventory() == true)
                    script.food!!.eat()
            }
            val currentCount = Inventory.getCount(id)
            if (Utils.walkAndInteract(gi, "Take") && (i == loots.size - 1 || gi.distance() >= 1)) {
                if (script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.id == id) {
                    waitFor(5000) { !isTargetDying() || script.loot().any { loot -> loot.id() != id } }
                }
                waitFor(5000) { currentCount < Inventory.getCount(gi.id()) || Players.local().tile() == gi.tile }
            }
        }
    }

    fun isTargetDying(): Boolean {
        val monster = script.getNearbyMonsters().firstOrNull()
        return monster != null && monster.healthBarVisible() && monster.healthPercent() == 0
    }
}