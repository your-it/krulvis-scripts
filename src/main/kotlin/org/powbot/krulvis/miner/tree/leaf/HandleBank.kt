package org.powbot.krulvis.miner.tree.leaf

import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.miner.Miner

class HandleBank(script: Miner) : Leaf<Miner>(script, "Handle Bank") {
    override fun execute() {
        if (!ctx.inventory.emptyExcept(*Data.TOOLS)) {
            ctx.bank.depositAllExcept(*Data.TOOLS)
        } else {
            ctx.bank.close()
        }
    }
}