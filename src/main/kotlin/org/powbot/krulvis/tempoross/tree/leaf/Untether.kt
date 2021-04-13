package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class Untether(script: Tempoross) : Leaf<Tempoross>(script, "Untethering") {
    override fun loop() {
        val pole = objects.firstOrNull { it.actions().contains("Untether") && it.distance() <= 2 }
        if (interact(pole, "Untether")) {
            waitFor { !script.isTethering() }
        }
    }


}