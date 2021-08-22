package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Tether
import org.powbot.krulvis.tempoross.tree.leaf.Untether

class ShouldUntether(script: Tempoross) : Branch<Tempoross>(script, "Should Untether") {
    override fun validate(): Boolean {
        return script.waveTimer.isFinished()
                && Objects.stream().action("Untether").within(3.0).firstOrNull() != null
    }

    override val successComponent: TreeComponent<Tempoross> = Untether(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldTether(script)
}

class ShouldTether(script: Tempoross) : Branch<Tempoross>(script, "Should Tether") {
    override fun validate(): Boolean {
        return !script.waveTimer.isFinished() && Inventory.containsOneOf(ROPE)
    }

    override val successComponent: TreeComponent<Tempoross> = Tether(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldSpec(script)
}