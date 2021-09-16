package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.CacheItemConfig
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.model.GameObjectInteraction
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner
import org.powbot.mobile.script.ScriptManager
import kotlin.system.measureTimeMillis

class HandleBank(script: Combiner) : Leaf<Combiner>(script, "Handle Bank") {
    override fun execute() {
        val keepItems = script.items.map { it.first }.toIntArray()
        if (!Bank.opened()) {
            Bank.openNearestBank()
        } else if (!Inventory.emptyExcept(*keepItems)) {
//            if (Inventory.stream().filtered { it.id !in keepItems }.list().map { it.id }.toSet().size > 1){
//                Bank.depositAll()
//            }
            Bank.depositAllExcept(*keepItems)
        } else {
            script.items.forEachIndexed { i, pair ->
                if ((i == script.items.size - 1 && Inventory.emptySlotCount() <= pair.second) || pair.second == 0) {
                    Bank.withdraw(pair.first, Bank.Amount.ALL)
                } else if (!Bank.withdrawExact(pair.first, pair.second, false)) {
                    return@forEachIndexed
                }
            }
            val outOfItem =
                script.items.firstOrNull {
                    (!Inventory.containsOneOf(it.first) || Inventory.getCount(it.first) < it.second)
                            && !Bank.containsOneOf(it.first)
                }
            if (outOfItem != null) {
                script.log.info("Out of: ${CacheItemConfig.load(outOfItem.first).name}, stopping script")
                ScriptManager.stop()
            }
        }
    }
}