package org.powbot.krulvis.slayer.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.tree.branch.ShouldEat
import org.powbot.krulvis.fighter.slayer.Slayer
//import org.powbot.krulvis.slayer.tree.leaf.HandleBank
//import org.powbot.krulvis.slayer.tree.leaf.OpenBank

//class ShouldBank(script: Slayer) : Branch<Slayer>(script, "Should Bank?") {
//    override val failedComponent: TreeComponent<Slayer> = ShouldEat(script, ShouldUseItem(script))
//    override val successComponent: TreeComponent<Slayer> = ShouldOpenBank(script)
//
//    override fun validate(): Boolean {
//        val hasRequirements = script.currentTask!!.target.requirements.all { it.meets() }
//        val bankForFood = (ShouldEat.needsFood() && !Food.hasFood())
//        if (!hasRequirements) {
//            script.log.info(
//                "Missing requirements=[${
//                    script.currentTask!!.target.requirements.filter { !it.meets() }.joinToString()
//                }]"
//            )
//            return true
//        } else if (bankForFood) {
//            script.log.info("Needs food=${ShouldEat.needsFood()}, Has Food=${Food.hasFood()}")
//            return true
//        }
//        return false
//    }
//
//}
//
//class ShouldOpenBank(script: Slayer) : Branch<Slayer>(script, "Should open Bank?") {
//    override val failedComponent: TreeComponent<Slayer> = HandleBank(script)
//    override val successComponent: TreeComponent<Slayer> = OpenBank(script)
//
//    override fun validate(): Boolean {
//        return !Bank.opened()
//    }
//}
