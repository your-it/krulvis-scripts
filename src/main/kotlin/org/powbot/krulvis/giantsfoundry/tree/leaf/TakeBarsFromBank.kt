package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class TakeBarsFromBank(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Take bars from bank") {

    override fun execute() {
        if (script.mouldWidgetOpen()) {
            val button = script.mouldWidget().firstOrNull { it?.text()?.contains("Set Mould") == true } ?: return
            button.click()
            waitFor { !script.mouldWidgetOpen() }
        } else if (openBank()) {
            script.barsToUse.forEach { (bar, amount) ->
                val curCount = Inventory.stream().id(bar).count().toInt()
                if (curCount <= amount) {
                    if (Bank.withdraw(bar, amount - curCount)) {
                        waitFor { Inventory.stream().id(bar).count().toInt() == amount }
                    }
                }
            }
        }
    }

    fun openBank(): Boolean {
        if (Bank.opened()) {
            return true
        }
        val bankObj = Objects.stream().name("Bank chest").firstOrNull() ?: return false
        if (bankObj.distance() > 3) Movement.step(bankObj.tile)
        return waitFor { bankObj.inViewport() } && bankObj.interact("Use") && waitFor { Bank.opened() }
    }
}