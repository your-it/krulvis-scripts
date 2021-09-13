package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Kill(script: Fighter) : Leaf<Fighter>(script, "Killing") {
    override fun execute() {
        val target = Npcs.stream().within(script.radius.toDouble()).name(*script.monsters.toTypedArray()).filtered {
            val target = it.interacting()
            target == null || target == Players.local()
        }.nearest().firstOrNull()
        if (target?.interact("Attack") == true) {
            waitFor { Players.local().interacting() == target || script.safespot != Players.local().tile() }
        }
    }
}