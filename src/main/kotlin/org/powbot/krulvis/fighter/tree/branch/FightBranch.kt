package org.powbot.krulvis.fighter.tree.branch

import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.fight.Attack
import org.powbot.krulvis.fighter.tree.leaf.fight.Chill
import org.powbot.krulvis.fighter.tree.leaf.fight.WalkToSpot
import org.powerbot.script.rt4.Npc

class AtSpot(script: Fighter) : Branch<Fighter>(script, "AtSpot") {
    override fun validate(): Boolean {
        val safeSpot = script.profile.safeSpot
        return if (safeSpot != null) {
            safeSpot.distance() <= script.profile.maxDistance
        } else {
            script.profile.centerLocation.distance() <= script.profile.radius
        }
    }

    override val successComponent: TreeComponent<Fighter> = IsKilling(script)
    override val failedComponent: TreeComponent<Fighter> = WalkToSpot(script)
}

class IsKilling(script: Fighter) : Branch<Fighter>(script, "IsKilling") {
    override fun validate(): Boolean {
        val target = me.interacting()
        debug("Currently interacting with: ${target.name()}")
        debug("Full debug: $target")
        return target is Npc && script.validTarget(target)
    }

    override val successComponent: TreeComponent<Fighter> = Chill(script)
    override val failedComponent: TreeComponent<Fighter> = Attack(script)
}

