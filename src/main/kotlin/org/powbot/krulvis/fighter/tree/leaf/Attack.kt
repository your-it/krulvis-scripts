package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.branch.Killing

class Attack(script:Fighter): Leaf<Fighter>(script,"Attacking") {
    override fun execute() {
        val target = script.target()
        if (script.hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0) {
            Prayer.quickPrayer(true)
        }
        if (attack(target)) {
            script.currentTarget = target
            Condition.wait({
                Killing.killing() || Players.local().animation() != -1
            }, 250, 10)
            if (script.shouldReturnToSafespot()) {
                Movement.step(script.centerTile(), 0)
            }
        }
    }
    fun attack(target: Npc?): Boolean {
        return if (script.useSafespot) {
            target?.interact("Attack") == true
        } else {
            ATContext.walkAndInteract(target, "Attack")
        }
    }
}