package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross
import java.awt.geom.Point2D.distance


class Untether(script: Tempoross) : Leaf<Tempoross>(script, "Untethering") {
    override fun execute() {
        val pole = ctx.objects.firstOrNull { it.actions().contains("Untether") && it.distance() <= 2 }
        if (interact(pole, "Untether")) {
            waitFor { !script.isTethering() }
        }
    }


}