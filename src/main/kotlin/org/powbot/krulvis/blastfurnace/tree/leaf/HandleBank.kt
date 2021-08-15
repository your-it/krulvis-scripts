package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.*

class HandleBank(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Handle bank") {

    override fun execute() {
        //Reset waiting at dispenser
        script.waitForBars = false
        if (Bank.opened()) {
            if (Inventory.containsOneOf(*Bar.values().map { it.itemId }.toIntArray())) {
                Bank.depositAllExcept(COAL_BAG, ICE_GLOVES, GOLD_GLOVES)
            } else if (!Inventory.isFull()) {
                Inventory.stream().id(COAL_BAG).findFirst().ifPresent {
                    it.interact("Fill")
                    script.filledCoalBag = true
                }

                if (Bank.withdraw(getNextOre(), Bank.Amount.ALL))
                    waitFor { Inventory.isFull() }
            }
        } else if (Chat.clickContinue()) {
            Objects.stream().name("Bank chest").action("Use").findFirst().ifPresent {
                if (interact(it, "Use")) {
                    waitFor(long()) { Bank.opened() }
                }
            }
        }

    }

    fun getNextOre(): Int {
        val coal = Ore.COAL.amount
        return when (script.bar) {
            Bar.STEEL -> Ore.IRON.itemId
            Bar.GOLD -> Ore.GOLD.itemId
            Bar.MITHRIL -> if (coal >= 27) Ore.MITH.itemId else Ore.COAL.itemId
            Bar.ADAMANTITE -> if (coal >= 54) Ore.ADAMANT.itemId else Ore.COAL.itemId
            Bar.RUNITE -> if (coal > 108) Ore.RUNE.itemId else Ore.COAL.itemId
            else -> Ore.COAL.itemId
        }
    }
}