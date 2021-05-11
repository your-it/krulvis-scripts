package org.powbot.krulvis.fighter.tree.leaf.bank

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class OpenBank(script: Fighter) : Leaf<Fighter>(script, "Opening Bank") {
    override fun execute() {
        ctx.bank.open()
    }
}