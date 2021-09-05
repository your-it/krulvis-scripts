package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Tile
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross


class EnterBoat(script: Tempoross) : Leaf<Tempoross>(script, "Entering boat") {
    override fun execute() {
        //Reset the side for the next run...
        script.side = Side.UNKNOWN

        val ropeLadder = script.getLadder()
        if ((ropeLadder?.distance() ?: 6) > 5) {
            debug("Walking first")
            walk(Tile(3137, 2841, 0))
        } else if (interact(ropeLadder, "Quick-climb")) {
            waitFor(long()) { BOAT_AREA.contains(ATContext.me.tile()) }
        }

    }
}