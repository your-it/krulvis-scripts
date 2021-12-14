package org.powbot.krulvis.slayer.tree.branch

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.slayer.Slayer
import org.powbot.krulvis.slayer.task.SlayerTarget

class ShouldUseItem(script: Slayer) : Branch<Slayer>(script, "Should use item?") {
    override val successComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Using item") {
        val killItem = script.currentTask!!.target.killItem()
        val currentXp = Skills.experience(Constants.SKILLS_SLAYER)
        if (Utils.walkAndInteract(
                Players.local().interacting(),
                "Use",
                false,
                true,
                killItem!!.ids[0]
            )
        ) {
            Condition.wait({ Skills.experience(Constants.SKILLS_SLAYER) > currentXp }, 250, 10)
        }
    }
    override val failedComponent: TreeComponent<Slayer> = Killing(script)

    var target: Npc? = null

    override fun validate(): Boolean {
        if (script.currentTask!!.target.killItem() == null)
            return false
        val interacting = Players.local().interacting()
        val percent = interacting.healthPercent()
        return interacting != Actor.Nil && percent <= 1
    }
}

class Killing(script: Slayer) : Branch<Slayer>(script, "Killing?") {
    override val failedComponent: TreeComponent<Slayer> = NearTarget(script)
    override val successComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Killing..") {
        val interacting = Players.local().interacting()
        Condition.wait { interacting == Actor.Nil || interacting.healthPercent() == 0 }
    }

    override fun validate(): Boolean {
        return killing(script.currentTask!!.target)
    }

    companion object {
        fun killing(target: SlayerTarget): Boolean {
            val interacting = Players.local().interacting()
            if (interacting == Actor.Nil) return false

            val hasKillItem = target.killItem() != null

            return interacting.healthBarVisible() && (hasKillItem || interacting.healthPercent() > 0)
        }
    }
}

class NearTarget(script: Slayer) : Branch<Slayer>(script, "Near target?") {
    override val successComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Attacking") {
        if (Utils.walkAndInteract(target, "Attack")) {
            Condition.wait({ Killing.killing(script.currentTask!!.target) }, 250, 15)
        }
    }
    override val failedComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Walking to spot") {
        Movement.walkTo(script.currentTask!!.location.centerTile)
    }

    var target: Npc? = null

    override fun validate(): Boolean {
        target = script.currentTask!!.target()
        return target != null
    }
}
