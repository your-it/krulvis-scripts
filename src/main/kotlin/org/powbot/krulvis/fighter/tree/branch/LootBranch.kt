package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Loot

class CanLoot(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = Loot(script)
    override val failedComponent: TreeComponent<Fighter> = AtSpot(script)

    override fun validate(): Boolean {
        return script.loot().isNotEmpty()
    }
}