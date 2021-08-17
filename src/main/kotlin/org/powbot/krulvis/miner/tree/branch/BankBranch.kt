package org.powbot.krulvis.miner.tree.branch

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner
import org.powbot.krulvis.miner.tree.leaf.*
import org.powbot.api.Tile
import org.powbot.api.rt4.*

class ShouldFixStrut(script: Miner) : Branch<Miner>(script, "Should fix strut") {

    override fun validate(): Boolean {
        val hasHammer = Inventory.containsOneOf(Item.HAMMER)
        val strutCount = Objects.stream(15).name("Broken strut").count().toInt()
        return (!Inventory.isFull() || hasHammer)
                && Tile(3746, 5667, 0).distance() <= 10
                && Npcs.stream().name("Pay-dirt").isNotEmpty()
                && (strutCount == 2 || (hasHammer && strutCount >= 1))
    }

    override val successComponent: TreeComponent<Miner> = FixStrut(script)
    override val failedComponent: TreeComponent<Miner> = ShouldBank(script)
}

class ShouldBank(script: Miner) : Branch<Miner>(script, "Should Bank") {

    override fun validate(): Boolean {
        //Some dirty hammer dropping
        val hammer = Inventory.stream().id(Item.HAMMER).findFirst()
        hammer.ifPresent { Game.tab(Game.Tab.INVENTORY) && it.interact("Drop") }

        //Actual should bank
        val full = Inventory.isFull()
        return full || Bank.opened() || DepositBox.opened()
                || (script.shouldEmptySack
                && !Inventory.emptyExcept(*Data.TOOLS)
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
        } else if (count == 0 && Inventory.emptyExcept(*Data.TOOLS)) {
            println("Sack is empty and inventory contains only tools")
            script.shouldEmptySack = false
        }
        if (script.shouldEmptySack) {
            println("Sack contains: $count pay-dirt")
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
        return Inventory.containsOneOf(*Ore.PAY_DIRT.ids)
    }

    override val successComponent: TreeComponent<Miner> = DropPayDirt(script)
    override val failedComponent: TreeComponent<Miner> = IsBankOpen(script)
}

class IsBankOpen(script: Miner) : Branch<Miner>(script, "Is Bank open") {

    override fun validate(): Boolean {
        return Bank.opened() || DepositBox.opened()
    }

    override val successComponent: TreeComponent<Miner> = HandleBank(script)
    override val failedComponent: TreeComponent<Miner> = SimpleLeaf(script, "OpenBank") {
        val nearestBank = Bank.getNearestBank(true)
        println("Opening: ${nearestBank.name}")
        nearestBank.open()
    }
}