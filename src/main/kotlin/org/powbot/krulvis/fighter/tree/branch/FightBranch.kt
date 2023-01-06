package org.powbot.krulvis.fighter.tree.branch

import kotlinx.coroutines.isActive
import org.powbot.api.Condition
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.slayer.*

class ShouldUseItem(script: Fighter) : Branch<Fighter>(script, "Should use item?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Using item") {
        val killItem = script.killItem()
        val currentXp = Skills.experience(Constants.SKILLS_SLAYER)
        if (Utils.walkAndInteract(
                script.currentTarget,
                "Use",
                false,
                true,
                killItem!!.ids[0]
            )
        ) {
            if (Condition.wait({ Skills.experience(Constants.SKILLS_SLAYER) > currentXp }, 300, 10)) {
                script.watchLootDrop(script.currentTarget!!.tile())
                script.currentTarget = null
            }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = Killing(script)

    override fun validate(): Boolean {
        val currentTarget = script.currentTarget
        if (currentTarget == null || !currentTarget.valid() || script.killItem() == null)
            return false
        return currentTarget.healthBarVisible() && currentTarget.healthPercent() <= 1
    }
}

class Killing(script: Fighter) : Branch<Fighter>(script, "Killing?") {
    override val failedComponent: TreeComponent<Fighter> = CanKill(script)
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Killing..") {
        Chat.clickContinue()
        if (script.hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0) {
            Prayer.quickPrayer(true)
        }

        val safespot = script.centerTile()
        if (Condition.wait { shouldReturnToSafespot() }) {
            if (shouldReturnToSafespot()) {
                Movement.step(safespot, 0)
            }
        }
    }

    fun shouldReturnToSafespot() =
        script.useSafespot && script.centerTile() != Players.local().tile() && Players.local().healthBarVisible()

    override fun validate(): Boolean {
        return killing(script.killItem())
    }

    companion object {
        fun killing(killItem: KillItemRequirement?): Boolean {
            val interacting = Players.local().interacting()
            if (interacting == Actor.Nil) return false
            if (!interacting.healthBarVisible()) return false
            return killItem != null || TargetWidget.health() > 0
        }


    }
}

class CanKill(script: Fighter) : Branch<Fighter>(script, "Can Kill?") {

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Attacking") {
        if (script.hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0) {
            Prayer.quickPrayer(true)
        }
        if (attack(target)) {
            script.currentTarget = target
            Condition.wait({
                Killing.killing(script.killItem())
            }, 250, 10)
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldSpawnTarget(script)

    var target: Npc? = null

    fun attack(target: Npc?): Boolean {
        return if (script.useSafespot) {
            target?.interact("Attack") == true
        } else {
            ATContext.walkAndInteract(target, "Attack")
        }
    }

    override fun validate(): Boolean {
        if (script.useSafespot && script.centerTile() != Players.local().tile()) return false
        target = script.target()
        return target != null
    }
}

class ShouldSpawnTarget(script: Fighter) : Branch<Fighter>(script, "Should spawn target?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Spawning") {
        val spawnItem = script.slayer.spawnItem()!!
        val id = spawnItem.ids[0]
        val name = when (id) {
            FISHING_EXPLOSIVE -> "Ominous Fishing Spot"
            SLAYER_BELL -> "Molanisk"
            else -> ""
        }
        val spot = Objects.stream().name(name).nearest().firstOrNull()
        if (spot != null && Utils.walkAndInteract(spot, "Use", false, true, id)) {
            Condition.wait({ script.slayer.currentTask!!.target() != null }, 400, 10)
        }
    }
    override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Walking to spot") {
        Chat.clickContinue()
        val spot = script.centerTile()
        val nearby = script.nearbyMonsters()
        if (nearby.none { it.reachable() } || (spot.distance() > if (script.useSafespot) 0 else script.radius))
            Movement.walkTo(spot)
    }

    override fun validate(): Boolean {
        return script.doSlayer && script.slayer.currentTask!!.location.centerTile.distance() < 10 && script.slayer.spawnItem() != null
    }
}