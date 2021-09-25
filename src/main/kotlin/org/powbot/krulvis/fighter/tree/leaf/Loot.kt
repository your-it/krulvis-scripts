package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Loot(script: Fighter) : Leaf<Fighter>(script, "Looting") {
    override fun execute() {
        val loots = script.loot()

        val id = loots.first().id()
        script.log.info(
            "Looting: ${id}, name=${loots.first().name()} price=${
                GrandExchange.getItemPrice(id)
            }, at=${loots.first().tile}"
        )
        loots.forEachIndexed { i, gi ->
            val id = gi.id()
            if ((!gi.stackable() || !Inventory.containsOneOf(id)) && Inventory.isFull() && script.food?.inInventory() == true) {
                script.food!!.eat()
            }
            val currentCount = Inventory.getCount(id)
            if (gi.interact("Take") && (i == loots.size - 1 || gi.distance() >= 1)) {
                if (script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.id == id) {
                    waitFor(5000) { !isTargetDying() || script.loot().any { loot -> loot.id() != id } }
                }
                waitFor { currentCount < Inventory.getCount(gi.id()) }
            }
        }
    }

    fun isTargetDying(): Boolean {
        val monster = script.getNearbyMonsters().firstOrNull()
        return monster != null && monster.healthBarVisible() && monster.healthPercent() == 0
    }
}