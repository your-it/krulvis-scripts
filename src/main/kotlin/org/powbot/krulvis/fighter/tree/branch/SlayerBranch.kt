package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.krulvis.fighter.tree.leaf.CheckTask
import org.powbot.krulvis.fighter.tree.leaf.GetTask
import org.powbot.mobile.script.ScriptManager

class ShouldStop(script: Fighter) : Branch<Fighter>(script, "Should stop?") {

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Stopping") {
        Notifications.showNotification("Stopped because task was finished!")
        ScriptManager.stop()
    }
    override val failedComponent: TreeComponent<Fighter> = HasTask(script)

    override fun validate(): Boolean {
        return script.lastTask && script.taskRemainder() <= 0
    }
}

class HasTask(script: Fighter) : Branch<Fighter>(script, "Has task?") {
    override val successComponent: TreeComponent<Fighter> = ShouldSipPotion(script, ShouldEquipAmmo(script))
    override val failedComponent: TreeComponent<Fighter> = ShouldGetTask(script)

    override fun validate(): Boolean {
        return !script.doSlayer || script.slayer.currentTask?.onGoing() == true
    }
}

class ShouldGetTask(script: Fighter) : Branch<Fighter>(script, "Should get task?") {
    override val successComponent: TreeComponent<Fighter> = GetTask(script)
    override val failedComponent: TreeComponent<Fighter> = CheckTask(script)

    override fun validate(): Boolean {
        return Slayer.taskRemainder() == 0
    }

}