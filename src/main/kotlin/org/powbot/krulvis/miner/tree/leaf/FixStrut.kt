package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.mid
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner
import org.powerbot.script.Tile

class FixStrut(script: Miner) : Leaf<Miner>(script, "Fixing strut") {
    override fun execute() {
        if (ctx.inventory.containsOneOf(Item.HAMMER)) {
            val sack = ctx.objects.toStream().name("Broken strut").nearest().findFirst()
            sack.ifPresent {
                val brokenStruts = ctx.objects.toStream(10).name("Broken strut").count()
                if (interact(it, "Hammer")) {
                    waitFor(mid() + it.distance() * 400) {
                        ctx.objects.toStream(10).name("Broken strut").count() < brokenStruts
                    }
                }
            }
        } else {
            println("Getting hammer")
            val crate = ctx.objects.toStream().at(Tile(3752, 5674, 0)).findFirst()
            crate.ifPresent {
                if (interact(it, "Search")) {
                    waitFor(5000) { ctx.inventory.containsOneOf(Item.HAMMER) }
                }
            }
        }
    }
}