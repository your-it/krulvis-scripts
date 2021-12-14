package org.powbot.krulvis.slayer.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.slayer.Slayer
import org.powbot.krulvis.slayer.tree.leaf.CheckTask
import org.powbot.krulvis.slayer.tree.leaf.GetTask

class HasTask(script: Slayer) : Branch<Slayer>(script, "Has task?") {
    override val successComponent: TreeComponent<Slayer> = ShouldBank(script)
    override val failedComponent: TreeComponent<Slayer> = ShouldGetTask(script)

    override fun validate(): Boolean {
        return script.currentTask?.onGoing() == true
    }

}

class ShouldGetTask(script: Slayer) : Branch<Slayer>(script, "Should get task?") {
    override val successComponent: TreeComponent<Slayer> = GetTask(script)
    override val failedComponent: TreeComponent<Slayer> = CheckTask(script)

    override fun validate(): Boolean {
        return Slayer.taskRemainder() == 0
    }

}