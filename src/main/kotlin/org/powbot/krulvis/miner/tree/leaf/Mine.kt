package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Ore.Companion.hasOre
import org.powbot.krulvis.api.extensions.items.Ore.Companion.mined
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.Miner

class Mine(script: Miner) : Leaf<Miner>(script, "Mining") {

    override fun execute() {
        val rock = Objects.stream().filtered {
            it.tile() in script.rockLocations && it.hasOre()
        }.nearest().firstOrNull()

        if (rock != null) {
            val path = LocalPathFinder.findPath(rock.tile().getWalkableNeighbor())
            if (path.containsSpecialNode()) {
                path.traverse()
            } else {
                if ((script.fastMine || script.mineDelay.isFinished()) && interact(rock, "Mine")) {
                    if (waitFor(long()) { me.animation() > 0 || rock.mined() }) {
                        script.mineDelay.resetTimer()
                    }
                }
            }
        }
    }
}