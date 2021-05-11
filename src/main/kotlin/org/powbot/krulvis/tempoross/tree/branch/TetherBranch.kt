package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Tether
import org.powbot.krulvis.tempoross.tree.leaf.Untether
import org.powerbot.script.rt4.GameObject

class ShouldUntether(script: Tempoross) : Branch<Tempoross>(script, "Should Untether") {
    override fun validate(): Boolean {
        return script.waveTimer.isFinished()
                && ctx.objects.toStream().action("Untether").within(2.0).first() != GameObject.NIL
    }

    override val successComponent: TreeComponent<Tempoross> = Untether(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldTether(script)
}

class ShouldTether(script: Tempoross) : Branch<Tempoross>(script, "Should Tether") {
    override fun validate(): Boolean {
        return !script.waveTimer.isFinished() && ctx.inventory.containsOneOf(ROPE)
    }
    
    override val successComponent: TreeComponent<Tempoross> = Tether(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldSpec(script)
}