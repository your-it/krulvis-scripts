package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross
import org.powerbot.script.rt4.Npc


class Leave(script: Tempoross) : Leaf<Tempoross>(script, "Leaving") {
    override fun loop() {
        script.blockedTiles.clear()
        script.triedPaths.clear()
        val leaveNpc = getLeaveNpc()
        if (leaveNpc != Npc.NIL) {
            println("Leaving island first..")
            if (interact(leaveNpc, "Leave")) {
                waitFor(5000) { getLeaveNpc() == Npc.NIL }
            }
        }
    }

    fun getLeaveNpc(): Npc = npcs.toStream().action("Leave").nearest().first()

}