package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.Input
import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES
import org.powbot.krulvis.blastfurnace.ICE_GLOVES

class AddCoffer(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Adding to coffer") {
    override fun execute() {
        val depositButton = Chat.stream().textContains("Deposit coins.").findFirst()
        if (Inventory.getCount(995) < script.cofferAmount) {
            script.log.info("Not enough gold yet...")
            if (!Bank.opened()) {
                val chest = Objects.stream().name("Bank chest").findFirst()
                chest.ifPresent { if (interact(it, "Use")) waitFor(long()) { Bank.opened() } }
            } else if (!Inventory.emptyExcept(COAL_BAG, ICE_GLOVES, GOLD_GLOVES)) {
                Bank.depositAllExcept(COAL_BAG, ICE_GLOVES, GOLD_GLOVES)
            } else if (Bank.withdraw(995, script.cofferAmount)) {
                waitFor { Inventory.getCount(995) >= script.cofferAmount }
            }
        } else if (depositButton.isPresent) {
            if (depositButton.get().select()) {
                waitFor(long()) { Chat.pendingInput() }
            }
        } else if (Chat.pendingInput()) {
            script.log.info("Pending deposit input...")
            Input.sendln("${script.cofferAmount / 1000}k")
            waitFor { script.cofferCount() > 100 }
        } else if (Bank.close()) {
            val matrix = Tile(1946, 4957, 0).matrix()
            if (script.interact(matrix, "Use")) {
                waitFor(long()) { chatOpen() }
            }
        }

    }

    fun chatOpen(): Boolean = Chat.stream().text("Deposit coins.").isNotEmpty()

}