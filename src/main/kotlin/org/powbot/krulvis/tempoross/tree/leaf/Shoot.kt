package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross
import org.powerbot.script.rt4.Npc


class Shoot(script: Tempoross) : Leaf<Tempoross>(script, "Shooting") {
    override fun loop() {
        val ammo = script.getAmmoCrate()
        val cooking = me.animation() == FILLING_ANIM && ammo.isPresent && ammo.get().distance() <= 2
        if (!cooking && script.interactWhileDousing(ammo, "Fill", script.mastLocation, false)) {
            waitFor { me.animation() == FILLING_ANIM }
        }
    }

}