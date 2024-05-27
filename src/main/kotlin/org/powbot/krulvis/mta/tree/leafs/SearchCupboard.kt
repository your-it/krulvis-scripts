package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.AlchemyRoom

class SearchCupboard(script: MTA) : Leaf<MTA>(script, "Searching cupboard") {
    override fun execute() {
        if (AlchemyRoom.order.isEmpty()) {
            val cupboards = AlchemyRoom.getAllCupboards()
            val nearest = cupboards.minBy { it.distance() }

            val inventory = Inventory.itemCounts()
            if (walkAndInteract(nearest, "Search")) {
                val index = cupboards.indexOf(nearest)
                waitFor { inventory != Inventory.itemCounts() }
                val foundItem = Inventory.itemCounts().firstOrNull { !inventory.contains(it) }?.first
                    ?: AlchemyRoom.Alchable.NONE.itemName
                val alchable = AlchemyRoom.Alchable.forName(foundItem)
                script.log.info("New item found=$alchable, at pole with id=${nearest.id}, and index=$index")

                val newOrder = buildOrder(alchable, index)
                script.log.info(newOrder.joinToString())
                AlchemyRoom.order = newOrder
            }
        } else {


        }
    }

    private fun buildOrder(alchable: AlchemyRoom.Alchable, cupboardIndex: Int): List<AlchemyRoom.Alchable> {
        val alchables = AlchemyRoom.Alchable.values()
        val knownAlchableIndex = alchable.ordinal
        val offset = (cupboardIndex - alchable.ordinal + alchables.size) % alchables.size
        script.log.info("CupboardIndex=$cupboardIndex, knownItemIndex=$knownAlchableIndex, offset=$offset")

        // Rotate the list based on the offset
        return alchables.drop(offset) + alchables.take(offset)
    }

    private fun Inventory.itemCounts() =
        Inventory.items().groupBy { it.name() }.map { it.key to it.value.sumOf { item -> item.stack } }
}