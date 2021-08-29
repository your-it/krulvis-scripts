package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner

class Drop(script: Miner) : Leaf<Miner>(script, "Drop") {

    override fun execute() {
        Inventory.dropExcept(*Data.TOOLS)
    }

    fun Inventory.dropExcept(vararg ids: Int): Boolean {
        if (!Game.tab(Game.Tab.INVENTORY)) {
            return false
        }
        if (stream().filter { it.id() !in ids }.isEmpty()) {
            return true
        }
        items().forEach {
            if (it.id() !in ids) {
                it.interact("Drop")
            }
        }
        return waitFor { stream().filter { it.id() !in ids }.isEmpty() }
    }
}