package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.InteractableEntity
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GameObject
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.leafs.HandleBank
import org.powbot.krulvis.runecrafting.tree.leafs.MoveToBank
import org.powbot.krulvis.runecrafting.tree.leafs.RepairPouches

class ShouldRepair(script: Runecrafter) : Branch<Runecrafter>(script, "Should repair?") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldBank(script)
    override val successComponent: TreeComponent<Runecrafter> = RepairPouches(script)

    override fun validate(): Boolean {
        return EssencePouch.inInventory().any { it.shouldRepair() }
    }
}

class ShouldBank(script: Runecrafter) : Branch<Runecrafter>(script, "Should bank?") {
    override val failedComponent: TreeComponent<Runecrafter> = AtAltar(script)
    override val successComponent: TreeComponent<Runecrafter> = ShouldOpenBank(script)

    override fun validate(): Boolean {
        return Bank.opened() || EssencePouch.inInventory().all { it.getCount() <= 0 } && EssencePouch.essenceCount() == 0
    }
}

class ShouldOpenBank(script: Runecrafter) : Branch<Runecrafter>(script, "Should open bank?") {
    override val failedComponent: TreeComponent<Runecrafter> = HandleBank(script)
    override val successComponent: TreeComponent<Runecrafter> = AtBank(script)

    override fun validate(): Boolean {
        return !Bank.opened()
    }
}


class AtBank(script: Runecrafter) : Branch<Runecrafter>(script, "At Bank?") {
    override val failedComponent: TreeComponent<Runecrafter> = MoveToBank(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Opening Bank") {
        Bank.open()
    }

    var bankObj: InteractableEntity = GameObject.Nil
    override fun validate(): Boolean {
        bankObj = Bank.getBank()
        return bankObj.valid()
    }
}