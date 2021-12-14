package org.powbot.krulvis.slayer.tree.branch

import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.slayer.Slayer
import org.powbot.krulvis.slayer.task.FISHING_EXPLOSIVE
import org.powbot.krulvis.slayer.task.SlayerTarget

class ShouldUseItem(script: Slayer) : Branch<Slayer>(script, "Should use item?") {
    override val successComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Using item") {
        val killItem = script.currentTask!!.target.killItem()
        val currentXp = Skills.experience(Constants.SKILLS_SLAYER)
        script.log.info("target=$target, item=${killItem!!.ids[0]}")
        if (Utils.walkAndInteract(
                target,
                "Use",
                false,
                true,
                killItem.ids[0]
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
        target = Npcs.stream().within(4.0).interactingWithMe()
            .firstOrNull { it.healthBarVisible() && it.healthPercent() <= 1 }
        return target != null
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
        when (spawnItem.ids[0]) {
            FISHING_EXPLOSIVE -> {
                val spot = Objects.stream().name("Ominous Fishing Spot").nearest().firstOrNull()
                if (spot != null && Utils.walkAndInteract(spot, "Use", false, true, FISHING_EXPLOSIVE)) {
                    Condition.wait({ script.currentTask!!.target() != null }, 250, 20)
                }
            }
        }
    }
    override val failedComponent: TreeComponent<Slayer> = SimpleLeaf(script, "Walking to spot") {
        Movement.walkTo(script.currentTask!!.location.centerTile)
    }

    override fun validate(): Boolean {
        return script.currentTask!!.location.centerTile.distance() < 10 && script.currentTask!!.target.spawnItem() != null
    }
}