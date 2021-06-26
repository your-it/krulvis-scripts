package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.runecrafter.PURE_ESSENCE
import org.powbot.krulvis.runecrafter.Runecrafter
import org.powerbot.script.rt4.Bank

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Open bank") {
    override fun execute() {
        if (ctx.bank.depositAllExcept(PURE_ESSENCE, script.profile.type.talisman)) {
            ctx.bank.withdraw(PURE_ESSENCE, Bank.Amount.ALL) { ctx.inventory.isFull }
        }
    }
}