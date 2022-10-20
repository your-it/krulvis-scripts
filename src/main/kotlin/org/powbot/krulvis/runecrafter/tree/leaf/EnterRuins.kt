package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafter.Runecrafter

class EnterRuins(script: Runecrafter) : Leaf<Runecrafter>(script, "Entering Ruins") {
    override fun execute() {
        val ruins = Objects.stream(25).name("Mysterious ruins").findFirst()
        ruins.ifPresent {
            val interaction = if (Inventory.containsOneOf(script.profile.type.talisman)) {
                walkAndInteract(it, "Use", selectItem = script.profile.type.talisman)
            } else {
                walkAndInteract(it, "Enter")
            }
            if (interaction) {
                waitFor { script.atAltar() }
            }
        }
    }
}