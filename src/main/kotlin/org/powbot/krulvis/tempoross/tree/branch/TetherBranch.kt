package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Tether
import org.powbot.krulvis.tempoross.tree.leaf.Untether
import org.powerbot.script.rt4.GameObject

class ShouldUntether(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        return script.waveTimer.isFinished()
                && objects.toStream().action("Untether").within(2.0).first() != GameObject.NIL
    }

    override fun onSuccess(): TreeComponent {
        return Untether(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldTether(script)
    }
}

class ShouldTether(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        return !script.waveTimer.isFinished() && inventory.contains(ROPE)
    }

    override fun onSuccess(): TreeComponent {
        return Tether(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldSpec(script)
    }
}