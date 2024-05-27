package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mta.rooms.AlchemyRoom
import org.powbot.krulvis.mta.MTA

class SearchCupboard(script: MTA) : Leaf<MTA>(script, "Searching cupboard") {
    override fun execute() {
        val cupboards = AlchemyRoom.getCupboard()
        script.log.info("Cupboards = $cupboards")
        val invCount = Inventory.emptySlotCount()
        if (walkAndInteract(cupboards, "Take-5")) {
            waitFor { Inventory.emptySlotCount() != invCount }
        }
    }
}