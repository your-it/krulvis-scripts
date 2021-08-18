package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.miner.Miner

class WalkToSpot(script: Miner) : Leaf<Miner>(script, "Walking to spot") {
    override fun execute() {
        val locs = script.rockLocations
        walk(locs[Random.nextInt(0, locs.size)])
    }
}