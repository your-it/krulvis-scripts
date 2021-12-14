package org.powbot.krulvis.slayer.task

import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.krulvis.slayer.Slayer

class SlayerTask(val target: SlayerTarget, val amount: Int, val location: Location) {

    fun onGoing() = Slayer.taskRemainder() > 0

    fun target(): Npc? =
        Npcs.stream().name(*target.names).filtered { !it.healthBarVisible() || it.healthPercent() > 0 }
            .reachable()
            .nearest()
            .firstOrNull()
}