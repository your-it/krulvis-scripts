package org.powbot.krulvis.gwd.saradomin.tree

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.gwd.leaf.Kill
import org.powbot.krulvis.gwd.saradomin.Saradomin

class CanAttack(script: Saradomin) : Branch<Saradomin>(script, "CanAttack") {
    override val failedComponent: TreeComponent<Saradomin>
        get() = TODO("Not yet implemented")
    override val successComponent = Kill(script, false)

    override fun validate(): Boolean {
        val me = me.trueTile()
        if (!script.zilyanaTiles.any { it == me }) {
            return false
        }

        successComponent.target = script.general
        val distance = script.general.trueTile().distanceTo(me)
        script.logger.info("On right tile, distance=$distance")
        return distance > 2
    }
}

class ShouldWalk(script: Saradomin) : Branch<Saradomin>(script, "ShouldWalk") {
    override val failedComponent: TreeComponent<Saradomin>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Saradomin>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }
}