package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES
import org.powbot.krulvis.blastfurnace.ICE_GLOVES

class PayForeman(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Pay Foreman") {
    override fun execute() {
        if (Inventory.getCount(995) < 2500) {
            val amount = if (script.cofferCount() < 150) 2500 + script.cofferAmount else 2500
            if (!Bank.opened()) {
                val chest = Objects.stream().name("Bank chest").findFirst()
                chest.ifPresent { if (interact(it, "Use")) waitFor(long()) { Bank.opened() } }
            } else if (!Inventory.emptyExcept(COAL_BAG, ICE_GLOVES, GOLD_GLOVES)) {
                Bank.depositAllExcept(COAL_BAG, ICE_GLOVES, GOLD_GLOVES)
            } else if (Bank.withdraw(995, amount)) {
                waitFor { Inventory.getCount(995) >= amount }
            }
        } else if (Chat.stream().textContains("Yes").isNotEmpty()) {
            Chat.stream().textContains("Yes").findFirst().ifPresent {
                val coins = Inventory.getCount(995)
                it.select()
                if (waitFor(long()) { coins > Inventory.getCount(995) }) {
                    script.foremanTimer.reset(10 * 60 * 1000)
                }
            }
        } else if (Bank.close()) {
            val foreman = Npcs.stream().name("Blast Furnace Foreman").findFirst()
            foreman.ifPresent {
                if (interact(it, "Pay")) {
                    waitFor(long()) { Chat.stream().textContains("Yes").isNotEmpty() }
                }
            }
        }

    }

}