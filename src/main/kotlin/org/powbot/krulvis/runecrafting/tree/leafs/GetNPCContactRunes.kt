package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.runecrafting.Runecrafter

class GetNPCContactRunes(script: Runecrafter) : Leaf<Runecrafter>(script, "Getting NPCContact runes") {
    override fun execute() {
        if (!Bank.open()) return
        val missingRunes = Magic.LunarSpell.NPC_CONTACT.requirements.filter { !it.meets() }
        missingRunes.map { rpr -> Pair(Rune.getForPower(rpr.power), rpr.amount) }.forEach { (r, a) ->
            if (Inventory.isFull()) Bank.deposit(script.essence, Bank.Amount.ALL)
            Bank.withdraw(r.id, a)
        }
        waitFor(1000) { Magic.LunarSpell.NPC_CONTACT.canCast() }
    }
}