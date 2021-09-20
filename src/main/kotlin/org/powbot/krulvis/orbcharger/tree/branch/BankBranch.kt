package org.powbot.krulvis.orbcharger.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.orbcharger.Orb
import org.powbot.krulvis.orbcharger.OrbCrafter
import org.powbot.krulvis.orbcharger.tree.leaf.HandleBank
import org.powbot.krulvis.orbcharger.tree.leaf.OpenBank

class ShouldBank(script: OrbCrafter) : Branch<OrbCrafter>(script, "ShouldBank?") {
    override val successComponent: TreeComponent<OrbCrafter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<OrbCrafter> = AtObelisk(script)

    override fun validate(): Boolean {
        return !Inventory.containsOneOf(Orb.UNPOWERED)
                || Inventory.getCount(Orb.COSMIC) < 3
                || !script.orb.staffEquipped()
    }
}

class IsBankOpen(script: OrbCrafter) : Branch<OrbCrafter>(script, "IsBankOpen?") {
    override val successComponent: TreeComponent<OrbCrafter> = HandleBank(script)
    override val failedComponent: TreeComponent<OrbCrafter> = OpenBank(script)

    override fun validate(): Boolean {
        return Bank.opened()
    }
}