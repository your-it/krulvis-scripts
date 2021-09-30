package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Kill(script: Fighter) : Leaf<Fighter>(script, "Killing") {
    override fun execute() {
        val target = script.target()
        if (target != null && script.hasPrayPots && !Prayer.quickPrayer()) {
            Prayer.quickPrayer(true)
        }
        if (attack(target)) {
            waitFor {
                Players.local().interacting() == target
                        || (script.useSafespot && script.safespot != Players.local().tile())
            }
        }
    }

    fun attack(target: Npc?): Boolean {
        return if (script.useSafespot) {
            target?.interact("Attack") == true
        } else {
            Utils.walkAndInteract(target, "Attack")
        }
    }
}