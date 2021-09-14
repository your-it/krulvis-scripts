package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Item.Companion.COINS
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES
import org.powbot.krulvis.blastfurnace.ICE_GLOVES

class DrinkPotion(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Draink potion") {
    override fun execute() {
        val pot = script.potion
        if (pot.needsRestore(60)) {
            if (!pot.inInventory()) {
                pot.withdrawExact(1, true)
            } else {
                if (pot.drink()) {
                    waitFor { !pot.needsRestore(60) }
                }
            }
        } else {
            Bank.depositAllExcept(COAL_BAG, GOLD_GLOVES, ICE_GLOVES, COINS)
        }
    }

}