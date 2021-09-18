package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager

class Walk(script: Woodcutter) : Leaf<Woodcutter>(script, "Chop Trees") {
    override fun execute() {
        val locs = script.trees
        script.chopDelay.forceFinish()
        if (locs.isEmpty()) {
            script.log.warning("Script requires at least 1 Tree GameObject set in the Configuration")
            ScriptManager.stop()
        }
    }
}