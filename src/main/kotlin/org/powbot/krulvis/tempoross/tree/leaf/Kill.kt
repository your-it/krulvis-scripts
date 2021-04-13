package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.KILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross


class Kill(script: Tempoross) : Leaf<Tempoross>(script, "Killing") {

    override fun loop() {
        script.forcedShooting = false
        val spirit = script.getBossPool()
        val killing = me.animation() == KILLING_ANIM && spirit.isPresent && spirit.get().distance() <= 3

        chat.canContinue()
        if (!killing && script.interactWhileDousing(spirit, "Harpoon", script.bossWalkLocation, true)) {
            waitFor { me.animation() == KILLING_ANIM }
        }
    }
}