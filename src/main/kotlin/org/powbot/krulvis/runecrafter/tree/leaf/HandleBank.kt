package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.runecrafter.PURE_ESSENCE
import org.powbot.krulvis.runecrafter.Runecrafter

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Open bank") {
    override fun execute() {
        if (Bank.depositAllExcept(PURE_ESSENCE, script.profile.type.talisman)) {
            Bank.withdraw(PURE_ESSENCE, Bank.Amount.ALL) { Inventory.isFull() }
        }
    }
}