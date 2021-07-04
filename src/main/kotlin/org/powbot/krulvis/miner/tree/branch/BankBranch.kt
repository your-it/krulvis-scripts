package org.powbot.krulvis.miner.tree.branch

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.Drop
import org.powbot.krulvis.miner.tree.leaf.DropPayDirt
import org.powbot.krulvis.miner.tree.leaf.EmptySack
import org.powbot.krulvis.miner.tree.leaf.HandleBank

class ShouldBank(script: Miner) : Branch<Miner>(script, "Should Bank") {

    override fun validate(): Boolean {
        return ctx.inventory.isFull || ctx.bank.opened() || ctx.depositBox.opened()
                || (script.shouldEmptySack && !ctx.inventory.emptyExcept(*Data.TOOLS))
    }

    override val successComponent: TreeComponent<Miner> = ShouldDrop(script)
    override val failedComponent: TreeComponent<Miner> = ShouldEmptySack(script)
}

class ShouldEmptySack(script: Miner) : Branch<Miner>(script, "Should empty sack") {

    override fun validate(): Boolean {
        val count = script.getMotherloadCount()
        if (count >= 81) {
            script.shouldEmptySack = true
        } else if (count <= 0) {
            script.shouldEmptySack = false
        }
        return script.shouldEmptySack && script.getSack().isPresent
    }

    override val successComponent: TreeComponent<Miner> = EmptySack(script)
    override val failedComponent: TreeComponent<Miner> = AtSpot(script)
}

class ShouldDrop(script: Miner) : Branch<Miner>(script, "Should Drop") {

    override fun validate(): Boolean {
        return script.profile.dropOres
    }

    override val successComponent: TreeComponent<Miner> = Drop(script)
    override val failedComponent: TreeComponent<Miner> = HasPayDirt(script)
}

class HasPayDirt(script: Miner) : Branch<Miner>(script, "Has pay-dirt") {

    override fun validate(): Boolean {
        return ctx.inventory.containsOneOf(*Ore.PAY_DIRT.ids)
    }

    override val successComponent: TreeComponent<Miner> = DropPayDirt(script)
    override val failedComponent: TreeComponent<Miner> = IsBankOpen(script)
}

class IsBankOpen(script: Miner) : Branch<Miner>(script, "Is Bank open") {

    override fun validate(): Boolean {
        return ctx.bank.opened() || ctx.depositBox.opened()
    }

    override val successComponent: TreeComponent<Miner> = HandleBank(script)
    override val failedComponent: TreeComponent<Miner> = SimpleLeaf(script, "OpenBank") {
        val nearestBank = ctx.bank.getNearestBank(true)
        println("Opening: ${nearestBank.name}")
        nearestBank.open()
    }
}