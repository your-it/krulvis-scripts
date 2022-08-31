package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafter.PURE_ESSENCE
import org.powbot.krulvis.runecrafter.Runecrafter
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Open bank") {
    override fun execute() {
        if (!Inventory.isFull() && !script.hasTalisman()) {
            val tiara = script.profile.type.tiara
            if (!Inventory.containsOneOf(tiara) && Bank.containsOneOf(tiara) && Bank.withdraw(tiara, 1)) {
                waitFor { Inventory.containsOneOf(tiara) }
            } else if (Inventory.containsOneOf(tiara)) {
                if (Inventory.stream().id(tiara).firstOrNull()?.interact("Wear") == true) {
                    waitFor { script.hasTalisman() }
                }
            } else if (Bank.containsOneOf(script.profile.type.talisman) && Bank.withdraw(
                    script.profile.type.talisman,
                    1
                )
            ) {
                waitFor { script.hasTalisman() }
            } else if (!Bank.containsOneOf(script.profile.type.talisman, tiara)) {
                script.log.info("No talisman/tiara, stopping script")
                ScriptManager.stop()
            }
        } else {
            if (!Inventory.emptyExcept(PURE_ESSENCE, script.profile.type.talisman)
                && Bank.depositAllExcept(PURE_ESSENCE, script.profile.type.talisman)
            ) {
                waitFor { Inventory.emptyExcept(PURE_ESSENCE, script.profile.type.talisman) }
            } else {
                Bank.withdraw(PURE_ESSENCE, Bank.Amount.ALL)
            }
        }
    }
}