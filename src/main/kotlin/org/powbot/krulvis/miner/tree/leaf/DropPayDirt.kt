package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class DropPayDirt(script: Miner) : Leaf<Miner>(script, "Drop pay-dirt") {

    override fun execute() {
        val hopper = getHopper()
        val totalPaydirt = Inventory.getCount(Ore.PAY_DIRT.id) + script.getMotherloadCount()
        if (hopper != null) {
            if (hopper.distance() > 3) {
                val path = LocalPathFinder.findPath(script.nearHopper)
                if (path.isNotEmpty()) {
                    println("Walking to hopper first! pathLength=${path.actions.size}")
                    path.traverse()
                } else {
                    walkWeb()
                }
            } else if (interact(hopper, "Deposit")) {
                script.lastPayDirtDrop = System.currentTimeMillis()
                sleep(600)
                waitFor(long()) {
                    deposited() && canWalkAway()
                }
            }
        } else {
            walkWeb()
        }
        if (totalPaydirt >= 81 && deposited()) {
            script.shouldEmptySack = true
        }
    }

    fun walkWeb() {
        if (script.escapeTopFloor()) {
            Movement.moveTo(script.nearHopper)
        }
    }

    fun canWalkAway(): Boolean {
        return script.getBrokenStrut() == null
                || Npcs.stream().name("Pay-dirt").isNotEmpty()
    }

    fun deposited() = Inventory.getCount(Ore.PAY_DIRT.id) == 0

    fun getHopper() = Objects.stream().name("Hopper").action("Deposit").firstOrNull()
}