package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.me
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross


class Shoot(script: Tempoross) : Leaf<Tempoross>(script, "Shooting") {
    override fun execute() {
        val ammo = script.getAmmoCrate()
        val dest = Movement.destination()
        val nearAmmoBox = ammo != null && (ammo.distance() <= 2 || ammo.tile().distanceTo(dest) <= 2)
        val shooting = me.animation() == FILLING_ANIM && nearAmmoBox
        if (!shooting && script.interactWhileDousing(ammo, "Fill", script.mastLocation, false)) {
            waitFor(long()) { me.animation() == FILLING_ANIM }
        }
    }

}