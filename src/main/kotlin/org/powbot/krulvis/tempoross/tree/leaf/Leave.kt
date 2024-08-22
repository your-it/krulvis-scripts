package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tempoross.Tempoross


class Leave(script: Tempoross) : Leaf<Tempoross>(script, "Leaving") {
    override fun execute() {
        script.burningTiles.clear()
        script.vulnerableStartHP = 100
        val leaveNpc = getLeaveNpc()
        if (leaveNpc != null && walkAndInteract(leaveNpc, "Leave")) {
            waitForDistance(leaveNpc, long()) { getLeaveNpc() == null }
        }
    }

    private fun getLeaveNpc(): Npc? = Npcs.stream().action("Leave").nearest(script.side.totemLocation).firstOrNull()

}