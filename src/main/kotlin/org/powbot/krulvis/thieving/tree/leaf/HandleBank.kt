package org.powbot.krulvis.thieving.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.maxHP
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thieving.Thiever

class HandleBank(script: Thiever) : Leaf<Thiever>(script, "Handle Bank") {
    override fun execute() {
        if (!Inventory.emptyExcept(*script.profile.food.ids)) {
            Bank.depositInventory()
            waitFor { !Inventory.isFull() }
        } else if (currentHP() < maxHP()) {
            if (script.profile.food.inInventory()) {
                val hp = currentHP()
                if (script.profile.food.eat()) {
                    waitFor(long()) { hp < currentHP() }
                }
            } else {
                if (Bank.withdraw(script.profile.food.getBankId(), Bank.Amount.FIVE)) {
                    waitFor { script.profile.food.inInventory() }
                }
            }
        }
    }
}