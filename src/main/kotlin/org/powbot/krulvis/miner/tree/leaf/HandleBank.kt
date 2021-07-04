package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner

class HandleBank(script: Miner) : Leaf<Miner>(script, "Handle Bank") {
    override fun execute() {
        if (!ctx.inventory.emptyExcept(*Data.TOOLS)) {
            ctx.bank.depositAllExcept(*Data.TOOLS)
            ctx.depositBox.depositAllExcept(*Data.TOOLS)
        } else {
            if (script.getMotherloadCount() == 0) {
                script.shouldEmptySack = true
            }
            ctx.bank.close()
            ctx.depositBox.close()
        }
    }
}