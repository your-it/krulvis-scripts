package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings
import org.powbot.krulvis.dagannothkings.Data
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king

class Attack(script: DagannothKings) : Leaf<DagannothKings>(script, "Attack") {

    private val attackTimer = Timer(2400)

    override fun execute() {
        val target = script.getNewTarget()
        val interactTile = me.tile()
        target.bounds(-32, 32, -222, -30, -32, 32)
        if (!target.valid() || !attackTimer.isFinished() || target.distance() > 14 || Movement.moving()) return

        val otherCloser = Npcs.stream()
            .filtered { it.name.contains("Dagannoth") && it.name != target.name && !it.dead() }
            .any { it.distance() < target.distance() || it.distanceTo(target) <= 4 }
        if (otherCloser) {
            script.logger.info("Not attacking because there's another closer")
        } else if (target.interact("Attack", target.name(), useMenu = true)) {
            attackTimer.reset()
            if (waitFor { me.interacting() == target } && target.king() == Data.King.Rex && target.tile().x < script.rexTile.x && script.lureTile.distance() > 0) {
                Movement.step(script.lureTile, 0)
            }
        } else if (waitFor { Movement.moving() }) {
            Movement.step(interactTile)
        }
    }
}