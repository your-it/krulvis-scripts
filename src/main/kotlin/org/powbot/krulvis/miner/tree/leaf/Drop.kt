package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner
import org.powerbot.script.rt4.Inventory

class Drop(script: Miner) : Leaf<Miner>(script, "Drop") {

    override fun execute() {
        ctx.inventory.dropExcept(*Data.TOOLS)
    }

    fun Inventory.dropExcept(vararg ids: Int): Boolean {
        if (toStream().filter { it.id() !in ids }.isEmpty()) {
            return true
        }
        ctx.inventory.items().forEach {
            if (it.id() !in ids) {
                it.interact("Drop")
            }
        }
        return waitFor { toStream().filter { it.id() !in ids }.isEmpty() }
    }
}