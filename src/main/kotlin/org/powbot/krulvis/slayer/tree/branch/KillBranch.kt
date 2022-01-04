//package org.powbot.krulvis.slayer.tree.branch
//
//import org.powbot.api.Condition
//import org.powbot.api.rt4.*
//import org.powbot.api.rt4.walking.local.Utils
//import org.powbot.api.script.tree.Branch
//import org.powbot.api.script.tree.SimpleLeaf
//import org.powbot.api.script.tree.TreeComponent
//import org.powbot.krulvis.fighter.Fighter
//import org.powbot.krulvis.fighter.slayer.*
//
//class ShouldUseItem(script: Fighter) : Branch<Fighter>(script, "Should use item?") {
//    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Using item") {
//        val killItem = script.killItem()
//        val currentXp = Skills.experience(Constants.SKILLS_SLAYER)
//        if (Utils.walkAndInteract(
//                script.currentTarget,
//                "Use",
//                false,
//                true,
//                killItem!!.ids[0]
//            )
//        ) {
//            script.waitForLoot(script.currentTarget!!.tile())
//            if (Condition.wait({ Skills.experience(Constants.SKILLS_SLAYER) > currentXp }, 250, 10)) {
//                script.currentTarget = null
//            }
//        }
//    }
//    override val failedComponent: TreeComponent<Fighter> = Killing(script)
//
//    override fun validate(): Boolean {
//        val currentTarget = script.currentTarget
//        if (currentTarget == null || !currentTarget.valid() || script.killItem() == null)
//            return false
//        return currentTarget.healthBarVisible() && currentTarget.healthPercent() <= 1
//    }
//}
//
//class Killing(script: Fighter) : Branch<Fighter>(script, "Killing?") {
//    override val failedComponent: TreeComponent<Fighter> = NearTarget(script)
//    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Killing..") {
//        val interacting = Players.local().interacting()
//        Chat.clickContinue()
//        if (script.hasPrayPots && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0) {
//            Prayer.quickPrayer(true)
//        }
//
//        if (Condition.wait { script.killItem() == null && (interacting == Actor.Nil || interacting.healthPercent() == 0) }) {
//            script.waitForLoot(interacting.tile())
//        }
//    }
//
//    override fun validate(): Boolean {
//        return killing(script.killItem())
//    }
//
//    companion object {
//        fun killing(killItem: KillItemRequirement?): Boolean {
//            val interacting = Players.local().interacting()
//            if (interacting == Actor.Nil) return false
//            if (!interacting.healthBarVisible()) return false
//            val hp = interacting.healthPercent()
//            return killItem != null || hp > 0
//        }
//    }
//}
//
//class NearTarget(script: Fighter) : Branch<Fighter>(script, "Near target?") {
//    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Attacking") {
//        if (Utils.walkAndInteract(target, "Attack")) {
//            script.currentTarget = target
//            Condition.wait({ Killing.killing(script.killItem()) }, 250, 15)
//        }
//    }
//    override val failedComponent: TreeComponent<Fighter> = ShouldSpawnTarget(script)
//
//    var target: Npc? = null
//
//    override fun validate(): Boolean {
//        target = script.target()
//        return target != null
//    }
//}
//
//class ShouldSpawnTarget(script: Fighter) : Branch<Fighter>(script, "Should spawn target?") {
//    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Spawning") {
//        val spawnItem = script.slayer.spawnItem()!!
//        val id = spawnItem.ids[0]
//        val name = when (id) {
//            FISHING_EXPLOSIVE -> "Ominous Fishing Spot"
//            SLAYER_BELL -> "Molanisk"
//            else -> ""
//        }
//        val spot = Objects.stream().name(name).nearest().firstOrNull()
//        if (spot != null && Utils.walkAndInteract(spot, "Use", false, true, id)) {
//            Condition.wait({ script.slayer.currentTask!!.target() != null }, 400, 10)
//        }
//    }
//    override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Walking to spot") {
//        Movement.walkTo(script.slayer.currentTask!!.location.centerTile)
//    }
//
//    override fun validate(): Boolean {
//        return script.doSlayer && script.slayer.currentTask!!.location.centerTile.distance() < 10 && script.slayer.spawnItem() != null
//    }
//}