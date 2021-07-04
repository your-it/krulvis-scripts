package org.powbot.krulvis.hunter.tree.branch

import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.hunter.Hunter
import org.powbot.krulvis.hunter.tree.leaf.DropLoot
import org.powbot.krulvis.hunter.tree.leaf.LootTrap
import org.powbot.krulvis.hunter.tree.leaf.PlaceTrap

class ShouldDrop(script: Hunter) : Branch<Hunter>(script, "Should drop loot") {
    override val successComponent: TreeComponent<Hunter> = DropLoot(script)
    override val failedComponent: TreeComponent<Hunter> = CanLootTrap(script)

    override fun validate(): Boolean {
        return ctx.inventory.isFull
    }
}

class CanLootTrap(script: Hunter) : Branch<Hunter>(script, "Can loot trap") {
    override val successComponent: TreeComponent<Hunter> = LootTrap(script)
    override val failedComponent: TreeComponent<Hunter> = CanPlaceTrap(script)

    override fun validate(): Boolean {
        return script.getFinishedTraps().isPresent
    }
}

class CanPlaceTrap(script: Hunter) : Branch<Hunter>(script, "Can place trap") {
    override val successComponent: TreeComponent<Hunter> = LootTrap(script)
    override val failedComponent: TreeComponent<Hunter> = PlaceTrap(script)

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }
}