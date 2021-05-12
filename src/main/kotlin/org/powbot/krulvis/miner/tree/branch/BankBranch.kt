package org.powbot.krulvis.miner.tree.branch

import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.Drop
import org.powbot.krulvis.miner.tree.leaf.HandleBank

class ShouldBank(script: Miner) : Branch<Miner>(script, "Should Bank") {

    override fun validate(): Boolean {
        return ctx.inventory.isFull || ctx.bank.opened()
    }

    override val successComponent: TreeComponent<Miner> = ShouldDrop(script)
    override val failedComponent: TreeComponent<Miner> = AtSpot(script)
}

class ShouldDrop(script: Miner) : Branch<Miner>(script, "Should Drop") {

    override fun validate(): Boolean {
        return script.profile.dropOres
    }

    override val successComponent: TreeComponent<Miner> = Drop(script)
    override val failedComponent: TreeComponent<Miner> = IsBankOpen(script)
}

class IsBankOpen(script: Miner) : Branch<Miner>(script, "Is Bank open") {

    override fun validate(): Boolean {
        return ctx.bank.opened()
    }

    override val successComponent: TreeComponent<Miner> = HandleBank(script)
    override val failedComponent: TreeComponent<Miner> = SimpleLeaf(script, "OpenBank") {
        val nearestBank = ctx.bank.getNearestBank()
        println("Opening: ${nearestBank.name}")
        nearestBank.open()
    }
}