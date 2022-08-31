package org.powbot.krulvis.runecrafter.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafter.PURE_ESSENCE
import org.powbot.krulvis.runecrafter.Runecrafter
import org.powbot.krulvis.runecrafter.tree.leaf.HandleBank
import org.powbot.krulvis.runecrafter.tree.leaf.OpenBank

class ShouldBank(script: Runecrafter) : Branch<Runecrafter>(script, "Should bank") {
    override val successComponent: TreeComponent<Runecrafter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Runecrafter> = AtAltar(script)

    override fun validate(): Boolean {
        return !script.hasTalisman() || !Inventory.containsOneOf(PURE_ESSENCE)
    }
}

class IsBankOpen(script: Runecrafter) : Branch<Runecrafter>(script, "Is bank open") {
    override val successComponent: TreeComponent<Runecrafter> = HandleBank(script)
    override val failedComponent: TreeComponent<Runecrafter> = OpenBank(script)

    override fun validate(): Boolean {
        return Bank.opened()
    }
}