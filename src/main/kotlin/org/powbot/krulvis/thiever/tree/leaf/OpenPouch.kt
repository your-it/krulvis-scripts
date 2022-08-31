package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever

class OpenPouch(script: Thiever) : Leaf<Thiever>(script, "Pouches") {
    override fun execute() {
        if (script.coinPouch()?.interact("Open-all") == true) {
            waitFor { script.coinPouch() == null }
        }
    }
}