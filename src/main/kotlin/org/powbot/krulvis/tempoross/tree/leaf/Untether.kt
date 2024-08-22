package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class Untether(script: Tempoross) : Leaf<Tempoross>(script, "Untethering") {
    override fun execute() {
        val closest = listOf(script.side.totemLocation, script.side.mastLocation).minByOrNull { it.distance() }!!
        script.logger.info("Untethering at ${closest.distance()}")
        if (closest.matrix().interact("Untether")) {
            waitFor { !script.isTethering() }
        }
    }


}