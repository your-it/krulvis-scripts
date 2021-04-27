package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross


class Cook(script: Tempoross) : Leaf<Tempoross>(script, "Cooking") {

    override fun execute() {
        val walkSpot = if (script.side == Tempoross.Side.NORTH) script.northCookSpot else script.cookLocation
        val cookShrine = ctx.objects.toStream().at(script.cookLocation).name("Shrine").findFirst()

        if (me.animation() != FILLING_ANIM) {
            if (script.interactWhileDousing(cookShrine, "Cook-at", walkSpot, false)) {
                waitFor(long()) { me.animation() == FILLING_ANIM }
            }
        } else if (me.animation() == FILLING_ANIM) {
            val tetherPole = script.getTetherPole()
            if (tetherPole.isPresent && !tetherPole.get().inViewport()) {
                ctx.camera.turnTo(tetherPole.get())
            }
        }
    }

}