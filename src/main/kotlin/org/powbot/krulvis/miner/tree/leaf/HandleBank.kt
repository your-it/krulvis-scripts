package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner

class HandleBank(script: Miner) : Leaf<Miner>(script, "Handle Bank") {
    override fun execute() {
        if (Inventory.stream().id(*Data.WATERSKINS, Data.EMPTY_WATERSKIN).isNotEmpty()) {
            script.waterskins = true
        }
        if (!Inventory.emptyExcept(*Data.TOOLS)) {
            if (Bank.opened()) {
                Bank.depositAllExcept(*Data.TOOLS)
            } else {
                DepositBox.depositAllExcept(*Data.TOOLS)
            }
        } else if (script.waterskins && Data.hasWaterSkins()) {
            Bank.withdraw(Data.WATERSKINS[0], Bank.Amount.FIVE)
        } else {
            Bank.close()
            DepositBox.close()
        }
    }
}