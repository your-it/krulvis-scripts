package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
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
        val target = script.getNewTarget() ?: return
        script.logger.info(" Going to attack ${target.king()}")
        target.bounds(-32, 32, -222, -30, -32, 32)
        if(target.king() == Data.King.Rex)
            attackRex(target)
    }

    fun attackRex(rex: Npc){
        val interactTile = me.tile()
        if (!rex.valid() || !attackTimer.isFinished() || rex.distance() > 14 || Movement.moving()) return

        val otherCloser = Npcs.stream()
            .filtered { it.name.contains("Dagannoth") && it.name != rex.name && !it.dead() }
            .any { it.distance() < rex.distance() || it.distanceTo(rex) <= 4 }
        if (otherCloser) {
            script.logger.info("Not attacking because there's another closer")
        } else if (rex.interact("Attack", rex.name(), useMenu = true)) {
            if (waitFor { me.interacting() == rex } && rex.king() == Data.King.Rex && rex.tile().x < script.rexTile.x && script.lureTile.distance() > 0) {
                Movement.step(script.lureTile, 0)
            }
            attackTimer.reset()
        } else if (waitFor { Movement.moving() }) {
            Movement.step(interactTile)
        }
    }
}