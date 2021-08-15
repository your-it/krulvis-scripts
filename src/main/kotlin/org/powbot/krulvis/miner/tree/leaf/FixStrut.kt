package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.mid
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects

class FixStrut(script: Miner) : Leaf<Miner>(script, "Fixing strut") {
    override fun execute() {
        if (Inventory.containsOneOf(Item.HAMMER)) {
            val sack = Objects.stream().name("Broken strut").nearest().findFirst()
            sack.ifPresent {
                val brokenStruts = Objects.stream(10).name("Broken strut").count()
                if (interact(it, "Hammer")) {
                    waitFor(mid() + it.distance() * 400) {
                        Objects.stream(10).name("Broken strut").count() < brokenStruts
                    }
                }
            }
        } else {
            println("Getting hammer")
            val crate = Objects.stream().at(Tile(3752, 5674, 0)).findFirst()
            crate.ifPresent {
                if (interact(it, "Search")) {
                    waitFor(5000) { Inventory.containsOneOf(Item.HAMMER) }
                }
            }
        }
    }
}