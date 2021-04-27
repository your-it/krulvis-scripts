package org.powbot.krulvis.fighter.tree.branch

import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.fight.Attack
import org.powbot.krulvis.fighter.tree.leaf.fight.Chill
import org.powbot.krulvis.fighter.tree.leaf.fight.WalkToSpot
import org.powerbot.script.rt4.Npc

class IsKilling(script: Fighter) : Branch<Fighter>(script, "IsKilling") {
    override fun validate(): Boolean {
        val target = me.interacting()
        return target is Npc && script.validTarget(target)
    }

    override val successComponent: TreeComponent<Fighter> = Chill(script)
    override val failedComponent: TreeComponent<Fighter> = AtSpot(script)
}

class AtSpot(script: Fighter) : Branch<Fighter>(script, "AtSpot") {
    override fun validate(): Boolean {
        return script.profile.centerLocation.distance() <= script.profile.radius
    }

    override val successComponent: TreeComponent<Fighter> = Attack(script)
    override val failedComponent: TreeComponent<Fighter> = WalkToSpot(script)
}