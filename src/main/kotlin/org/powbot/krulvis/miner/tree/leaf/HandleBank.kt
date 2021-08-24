package org.powbot.krulvis.miner.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner

class HandleBank(script: Miner) : Leaf<Miner>(script, "Handle Bank") {
    override fun execute() {
        if (!Inventory.emptyExcept(*Data.TOOLS)) {
            if (Bank.opened()) {
                Bank.depositAllExcept(*Data.TOOLS)
            } else {
                DepositBox.depositAllExcept(*Data.TOOLS)
            }
        } else {
            Bank.close()
            DepositBox.close()
        }
    }
}