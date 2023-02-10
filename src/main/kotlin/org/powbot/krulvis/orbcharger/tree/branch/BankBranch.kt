package org.powbot.krulvis.orbcharger.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.api.rt4.magic.RunePower
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.orbcharger.Orb
import org.powbot.krulvis.orbcharger.OrbCrafter
import org.powbot.krulvis.orbcharger.tree.leaf.EatFoodAtBank
import org.powbot.krulvis.orbcharger.tree.leaf.HandleBank
import org.powbot.krulvis.orbcharger.tree.leaf.OpenBank

class ShouldBank(script: OrbCrafter) : Branch<OrbCrafter>(script, "ShouldBank?") {
    override val successComponent: TreeComponent<OrbCrafter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<OrbCrafter> = AtObelisk(script)

    fun hasCosmics(): Boolean {
        if (Rune.COSMIC.inventoryCount() >= 3) {
            return true
        }
        val cosmicsInPouch = RunePouch.runes().firstOrNull { it.first == Rune.COSMIC } ?: return false
        return cosmicsInPouch.second >= 3
    }

    override fun validate(): Boolean {
        return !Inventory.containsOneOf(Orb.UNPOWERED)
                || !hasCosmics()
                || !script.orb.staffEquipped()
    }
}

class IsBankOpen(script: OrbCrafter) : Branch<OrbCrafter>(script, "IsBankOpen?") {
    override val successComponent: TreeComponent<OrbCrafter> = ShouldEatFood(script)
    override val failedComponent: TreeComponent<OrbCrafter> = OpenBank(script)

    override fun validate(): Boolean {
        return Bank.opened()
    }
}

class ShouldEatFood(script: OrbCrafter) : Branch<OrbCrafter>(script, "IsBankOpen?") {
    override val successComponent: TreeComponent<OrbCrafter> = EatFoodAtBank(script)
    override val failedComponent: TreeComponent<OrbCrafter> = HandleBank(script)

    override fun validate(): Boolean {
        return missingHP() >= 10
    }
}