package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.me
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross


class Cook(script: Tempoross) : Leaf<Tempoross>(script, "Cooking") {

    override fun execute() {
        val walkSpot = if (script.side == Tempoross.Side.NORTH) script.northCookSpot else script.cookLocation
        val cookShrine = Objects.stream().at(script.cookLocation).name("Shrine").findFirst()

        if (me.animation() != FILLING_ANIM) {
            if (script.interactWhileDousing(cookShrine, "Cook-at", walkSpot, false)) {
                waitFor(long()) { me.animation() == FILLING_ANIM }
            }
        } else if (me.animation() == FILLING_ANIM) {
            val tetherPole = script.getTetherPole()
            if (tetherPole.isPresent && !tetherPole.get().inViewport()) {
                Camera.turnTo(tetherPole.get())
            }
        }
    }

}