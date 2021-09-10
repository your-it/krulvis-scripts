package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Loot(script: Fighter) : Leaf<Fighter>(script, "Killing") {
    override fun execute() {
        val loots = script.loot()

        loots.forEachIndexed { i, gi ->
            if ((!gi.stackable() || !Inventory.containsOneOf(gi.id())) && Inventory.isFull() && script.food.inInventory()) {
                script.food.eat()
            }
            val currentCount = Inventory.getCount(gi.id())
            if (gi.interact("Take") && i == loots.size - 1) {
                waitFor { currentCount < Inventory.getCount(gi.id()) }
            }
        }
    }
}