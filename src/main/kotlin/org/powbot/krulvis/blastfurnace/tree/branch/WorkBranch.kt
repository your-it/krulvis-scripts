package org.powbot.krulvis.blastfurnace.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.Bar
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.tree.leaf.HandleBank
import org.powbot.krulvis.blastfurnace.tree.leaf.PutOre
import org.powbot.krulvis.blastfurnace.tree.leaf.TakeBars

class ShouldTake(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should take bars") {
    override val successComponent: TreeComponent<BlastFurnace> = TakeBars(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldPutOre(script)

    override fun validate(): Boolean {
        return script.bar.amount > 0
                && !Inventory.containsOneOf(
            script.bar.primary.itemId,
            script.bar.secondary.itemId
        )
    }
}

class ShouldPutOre(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Put Ore") {
    override val successComponent: TreeComponent<BlastFurnace> = PutOre(script)
    override val failedComponent: TreeComponent<BlastFurnace> = ShouldWaitAtDispenser(script)

    override fun validate(): Boolean {
        return Inventory.containsOneOf(script.bar.primary.itemId, script.bar.secondary.itemId)
    }
}

class ShouldWaitAtDispenser(script: BlastFurnace) : Branch<BlastFurnace>(script, "Should Wait @ Dispenser") {
    override val successComponent: TreeComponent<BlastFurnace> = SimpleLeaf(script, "Waiting for XP drop") {
        if (script.dispenserTile.distance() > 1) {
            walk(script.dispenserTile)
            waitFor { script.dispenserTile.distance() <= 1 }
        }
    }
    override val failedComponent: TreeComponent<BlastFurnace> = HandleBank(script)

    override fun validate(): Boolean {
        if (Inventory.containsOneOf(script.bar.itemId)) {
            script.waitForBars = false
        }
        return !Inventory.isFull() && script.waitForBars
    }
}