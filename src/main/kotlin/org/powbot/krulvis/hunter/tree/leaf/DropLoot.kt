package org.powbot.krulvis.hunter.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.hunter.Data.DROPPABLES
import org.powbot.krulvis.hunter.Hunter

class DropLoot(script: Hunter) : Leaf<Hunter>(script, "Drop loot") {
    override fun execute() {
        val droppables = ctx.inventory.toStream().id(*DROPPABLES).list()
        droppables.forEach {
            it.interact("Drop")
        }
    }
}