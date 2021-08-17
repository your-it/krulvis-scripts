package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.extensions.items.Ore.Companion.hasOre
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class Mine(script: Miner) : Leaf<Miner>(script, "Mining") {

    val mineDelay = DelayHandler(2000, script.oddsModifier, "MineDelay")

    override fun execute() {
        val rock = Objects.stream()
            .within(script.profile.center, script.profile.radius.toDouble())
            .filter {
                it.tile() in script.profile.oreLocations && it.hasOre()
            }.nearest().findFirst()
        rock.ifPresent {
            val path = LocalPathFinder.findPath(it.tile().getWalkableNeighbor())
            if (path.containsSpecialNode()) {
                path.traverse()
            } else {
                if ((mineDelay.isFinished() || script.profile.fastMining)
                    && interact(rock.get(), "Mine")
                ) {
                    debug("Interacted with")
                    if (waitFor(long()) { me.animation() > 0 }) {
                        mineDelay.resetTimer()
                    }
                } else {
                    debug("Failed to interact")
                }
            }
        }
    }
}