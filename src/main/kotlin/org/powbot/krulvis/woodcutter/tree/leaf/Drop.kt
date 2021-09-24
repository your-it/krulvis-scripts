package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter

class Drop(script: Woodcutter) : Leaf<Woodcutter>(script, "Dropping") {
    override fun execute() {
        val items = Inventory.stream().id(*script.LOGS).list()
        items.forEach {
            it.click("Drop")
            sleep(100, 200)
        }
        waitFor { Inventory.stream().id(*script.LOGS).isEmpty() }
    }
}