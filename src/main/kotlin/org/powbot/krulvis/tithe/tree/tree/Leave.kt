package org.powbot.krulvis.tithe.tree.tree

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Data.EMPTY_CAN
import org.powbot.krulvis.tithe.TitheFarmer
import org.powerbot.script.rt4.Game
import org.powerbot.script.rt4.GameObject
import java.util.*

class Leave(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Leaving") {

    override fun execute() {
        ctx.objects.toStream(25).name("Farm door").nearest().findFirst().ifPresent {
            if (interact(it, "Open")) {
                waitFor(long()) { script.getPoints() == -1 }
            }
        }
    }
}