package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.distanceM
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.walking.PBWebWalkingService

class WalkToSpot(script: Miner) : Leaf<Miner>(script, "Walking to spot") {
    override fun execute() {
        val distance = script.profile.center.distanceM(me.tile())
        println("Distance to center: $distance")
        if (distance <= 20) {
            LocalPathFinder.findPath(script.profile.center).traverse()
        } else {
            PBWebWalkingService.walkTo(script.profile.center, false)
        }
    }
}