package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever
import java.lang.Integer.min
import kotlin.math.ceil
import kotlin.math.max

class HandleBank(script: Thiever) : Leaf<Thiever>(script, "Handle Bank") {
    override fun execute() {
        val extra = ceil(missingHP() / script.food.healing.toDouble()).toInt()
        val toTake = min(28, script.foodAmount + extra)
        if (!Inventory.emptyExcept(*script.food.ids)) {
            Bank.depositInventory()
            waitFor { !Inventory.isFull() }
        } else if (script.food.getInventoryCount() < toTake) {
            script.log.info("Taking out: $toTake, extra=$extra")
            if (Bank.withdraw(script.food.getBankId(), toTake)) {
                waitFor { script.food.inInventory() }
            }
        } else if (script.dodgyNeck && !script.dodgy.hasWith()) {
            if (!script.dodgy.withdrawAndEquip(false)) {
                if (!script.dodgy.inInventory() && !script.dodgy.inBank()) {
                    script.dodgyNeck = false
                }
            }
        }
    }
}