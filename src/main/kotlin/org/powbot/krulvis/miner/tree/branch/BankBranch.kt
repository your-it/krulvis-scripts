package org.powbot.krulvis.miner.tree.branch

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.*
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Game

class ShouldFixStrut(script: Miner) : Branch<Miner>(script, "Should fix strut") {

    override fun validate(): Boolean {
        val hasHammer = ctx.inventory.containsOneOf(Item.HAMMER)
        val strutCount = ctx.objects.toStream(15).name("Broken strut").count().toInt()
        return !ctx.inventory.isFull
                && Tile(3746, 5667, 0).distance() <= 10
                && ctx.npcs.toStream().name("Pay-dirt").isNotEmpty()
                && (strutCount == 2 || (hasHammer && strutCount >= 1))
    }

    override val successComponent: TreeComponent<Miner> = FixStrut(script)
    override val failedComponent: TreeComponent<Miner> = ShouldBank(script)
}

class ShouldBank(script: Miner) : Branch<Miner>(script, "Should Bank") {

    override fun validate(): Boolean {
        //Some dirty hammer dropping
        val hammer = ctx.inventory.toStream().id(Item.HAMMER).findFirst()
        hammer.ifPresent { ctx.game.tab(Game.Tab.INVENTORY) && it.interact("Drop") }

        //Actual should bank
        val full = ctx.inventory.isFull
        return full || ctx.bank.opened() || ctx.depositBox.opened()
                || (script.shouldEmptySack
                && !ctx.inventory.emptyExcept(*Data.TOOLS)
                && (full || script.getMotherloadCount() == 0))
    }

    override val successComponent: TreeComponent<Miner> = ShouldDrop(script)
    override val failedComponent: TreeComponent<Miner> = ShouldEmptySack(script)
}

class ShouldEmptySack(script: Miner) : Branch<Miner>(script, "Should empty sack") {

    override fun validate(): Boolean {
        val count = script.getMotherloadCount()
        if (count >= 81) {
            script.shouldEmptySack = true
        } else if (count == 0 && ctx.inventory.emptyExcept(*Data.TOOLS)) {
            println("Sack is empty and inventory contains only tools")
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