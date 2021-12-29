package org.powbot.krulvis.slayer.tree.branch

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.slayer.Slayer
import org.powbot.krulvis.slayer.task.FISHING_EXPLOSIVE
import org.powbot.krulvis.slayer.task.SLAYER_BELL
import org.powbot.krulvis.slayer.task.SlayerTarget

class ShouldUseItem(script: Slayer) : Branch<Slayer>(script, "Should use item?") {
    override val successComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Using item") {
        val killItem = script.currentTask!!.target.killItem()
        val currentXp = Skills.experience(Constants.SKILLS_SLAYER)
        if (Utils.walkAndInteract(
                script.currentTarget,
                "Use",
                false,
                true,
                killItem!!.ids[0]
            )
        ) {
            if (Condition.wait({ Skills.experience(Constants.SKILLS_SLAYER) > currentXp }, 250, 10)) {
                script.currentTarget = null
            }
        }
    }
    override val failedComponent: TreeComponent<Slayer> = Killing(script)

    override fun validate(): Boolean {
        val currentTarget = script.currentTarget
        if (currentTarget == null || !currentTarget.valid() || script.currentTask!!.target.killItem() == null)
            return false
        return currentTarget.healthBarVisible() && currentTarget.healthPercent() <= 1
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
            script.currentTarget = target
            Condition.wait({ Killing.killing(script.currentTask!!.target) }, 250, 15)
        }
    }
    override val failedComponent: TreeComponent<Slayer> = ShouldSpawnTarget(script)

    var target: Npc? = null

    override fun validate(): Boolean {
        target = script.currentTask!!.target()
        return target != null
    }
}

class ShouldSpawnTarget(script: Slayer) : Branch<Slayer>(script, "Should spawn target?") {
    override val successComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Spawning") {
        val spawnItem = script.currentTask!!.target.spawnItem()!!
        val id = spawnItem.ids[0]
        val name = when (id) {
            FISHING_EXPLOSIVE -> "Ominous Fishing Spot"
            SLAYER_BELL -> "Molanisk"
            else -> ""
        }
        val spot = Objects.stream().name(name).nearest().firstOrNull()
        if (spot != null && Utils.walkAndInteract(spot, "Use", false, true, id)) {
            Condition.wait({ script.currentTask!!.target() != null }, 250, 20)
        }
    }
    override val failedComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Walking to spot") {
        Movement.walkTo(script.currentTask!!.location.centerTile)
    }

    override fun validate(): Boolean {
        return script.currentTask!!.location.centerTile.distance() < 10 && script.currentTask!!.target.spawnItem() != null
    }
}