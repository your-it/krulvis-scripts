package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever

class OpenPouch(script: Thiever) : Leaf<Thiever>(script, "Pouches") {
    override fun execute() {
        if (Inventory.stream().name("Coin pouch").firstOrNull()?.interact("Open-all") == true) {
            waitFor { Inventory.stream().name("Coin pouch").isEmpty() }
        }
    }
}