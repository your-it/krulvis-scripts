package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mta.GraveyardRoom
import org.powbot.krulvis.mta.GraveyardRoom.GRAVEYARD_METHOD
import org.powbot.krulvis.mta.GraveyardRoom.getEdible
import org.powbot.krulvis.mta.GraveyardRoom.getFruitCount
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.tree.leafs.DepositBones
import org.powbot.krulvis.mta.tree.leafs.TakeBones

class ShouldGraveyard(script: MTA) : Branch<MTA>(script, "G") {
    override val failedComponent: TreeComponent<MTA> = InsideTelekinesis(script)
    override val successComponent: TreeComponent<MTA> = CanDeposit(script)

    override fun validate(): Boolean {
        return script.method == GRAVEYARD_METHOD
    }
}

class CanDeposit(script: MTA) : Branch<MTA>(script, "Can convert bones?") {
    override val failedComponent: TreeComponent<MTA> = CanConvertBones(script)
    override val successComponent: TreeComponent<MTA> = DepositBones(script)

    override fun validate(): Boolean {
        return Inventory.isFull() && getEdible().valid()
    }
}

class CanConvertBones(script: MTA) : Branch<MTA>(script, "Can convert bones?") {
    override val failedComponent: TreeComponent<MTA> = TakeBones(script)
    override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Converting bones") {
        if (GraveyardRoom.getSpell().cast()) {
            waitFor { GraveyardRoom.getBoneItems().isEmpty() }
        }
    }

    override fun validate(): Boolean {
        val bones = GraveyardRoom.getBoneItems()
        val space = bones.count() + Inventory.emptySlotCount()
        return bones.getFruitCount() >= space
    }
}

