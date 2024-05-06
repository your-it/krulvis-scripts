package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Handling Bank") {
    override fun execute() {
        val rune = Inventory.stream().nameContains(script.alter.name).firstOrNull()
        if (rune != null) {
            Bank.deposit(rune.id, Bank.Amount.ALL)
        } else if (EssencePouch.inInventory().all { it.filled() } && Inventory.isFull()) {
            Bank.close()
        } else {
            if (!Inventory.isFull() && withdrawEssence()) {
                waitFor { Inventory.isFull() }
            }
            EssencePouch.inInventory().forEach { it.fill() }
            withdrawEssence()
        }
    }

    private fun withdrawEssence(): Boolean = Bank.withdraw(script.essence, Bank.Amount.ALL)

}