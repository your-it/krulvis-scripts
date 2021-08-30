package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.*
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Handle bank") {

    override fun execute() {
        //Reset waiting at dispenser
        script.waitForBars = false
        if (Bank.opened()) {
            if (Inventory.containsOneOf(*Bar.values().map { it.id }.toIntArray())) {
                Bank.depositAllExcept(COAL_BAG, ICE_GLOVES, GOLD_GLOVES)
            } else if (!Inventory.isFull()) {
                if (!Inventory.containsOneOf(ICE_GLOVES)
                    && !Equipment.containsOneOf(ICE_GLOVES)
                    && Bank.containsOneOf(ICE_GLOVES)
                ) {
                    Bank.withdraw(ICE_GLOVES, 1)
                }
                if (canUseCoalBag() && !Inventory.containsOneOf(COAL_BAG) && Bank.containsOneOf(COAL_BAG)) {
                    Bank.withdraw(COAL_BAG, 1)
                }
                if (script.bar == Bar.GOLD && !Inventory.containsOneOf(GOLD_GLOVES)
                    && !Equipment.containsOneOf(GOLD_GLOVES)
                    && Bank.containsOneOf(GOLD_GLOVES)
                ) {
                    Bank.withdraw(GOLD_GLOVES, 1)
                }
                Inventory.stream().id(COAL_BAG).findFirst().ifPresent {
                    it.interact("Fill")
                    script.filledCoalBag = true
                }

                val nextOre = getNextOre()

                if (Bank.withdraw(nextOre, Bank.Amount.ALL))
                    waitFor { Inventory.isFull() }

                if (!Inventory.isFull() && !Bank.containsOneOf(nextOre)) {
                    script.log.info("No more ores in bank, stopping script")
                    ScriptManager.stop()
                }
            }
        } else if (Chat.clickContinue()) {
            Objects.stream().name("Bank chest").action("Use").findFirst().ifPresent {
                if (interact(it, "Use")) {
                    waitFor(long()) { Bank.opened() }
                }
            }
        }

    }

    fun canUseCoalBag(): Boolean = script.bar.secondaryMultiplier > 0

    fun getNextOre(): Int {
        val coal = Ore.COAL.blastFurnaceCount
        val hasCoalBag = Inventory.containsOneOf(COAL_BAG)
        return when (script.bar) {
            Bar.GOLD -> Ore.GOLD.id
            Bar.STEEL -> if (coal >= if (hasCoalBag) 27 else 28) Ore.IRON.id else Ore.COAL.id
            Bar.MITHRIL -> if (coal >= if (hasCoalBag) 27 else 56) Ore.MITHRIL.id else Ore.COAL.id
            Bar.ADAMANTITE -> if (coal >= if (hasCoalBag) 54 else 84) Ore.ADAMANTITE.id else Ore.COAL.id
            Bar.RUNITE -> if (coal >= if (hasCoalBag) 108 else 112) Ore.RUNITE.id else Ore.COAL.id
            else -> Ore.COAL.id
        }
    }
}