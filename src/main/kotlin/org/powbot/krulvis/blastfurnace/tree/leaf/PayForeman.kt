package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace

class PayForeman(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Pay Foreman") {
    override fun execute() {
        if (Inventory.getCount(995) < 2500) {
            if (!Bank.opened()) {
                val chest = Objects.stream().name("Bank chest").findFirst()
                chest.ifPresent { if (interact(it, "Use")) waitFor { Bank.opened() } }
            }else if(Bank.withdraw(995, 12500)){
                waitFor { Inventory.getCount(995) >= 12500 }
            }
        }else if (Chat.stream().textContains("Yes").isNotEmpty()) {
            Chat.stream().textContains("Yes").findFirst().ifPresent {
                val coins = Inventory.getCount(995)
                it.select()
                if (waitFor(long()) { coins > Inventory.getCount(995) }) {
                    script.foremanTimer.reset(10 * 60 * 1000)
                }
            }
        } else {
            val foreman = Npcs.stream().name("Blast Furnace Foreman").findFirst()
            foreman.ifPresent {
                if (interact(it, "Pay")) {
                    waitFor(long()) { Chat.stream().textContains("Yes").isNotEmpty() }
                }
            }
        }

    }

}