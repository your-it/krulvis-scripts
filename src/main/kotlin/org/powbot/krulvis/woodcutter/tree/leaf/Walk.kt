package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager

class Walk(script: Woodcutter) : Leaf<Woodcutter>(script, "Walk to Trees") {
    override fun execute() {
        val locs = script.trees
        script.chopDelay.forceFinish()
        if (locs.isEmpty()) {
            script.log.warning("Script requires at least 1 Tree GameObject set in the Configuration")
            ScriptManager.stop()
        } else {
            val tile = locs.minByOrNull { it.distance() }
            if (tile != null && tile.distance() < 15) {
                Movement.step(tile)
                waitFor(long()) { tile.distance() < 5 }
            } else {
                Movement.walkTo(tile)
            }
        }
    }
}