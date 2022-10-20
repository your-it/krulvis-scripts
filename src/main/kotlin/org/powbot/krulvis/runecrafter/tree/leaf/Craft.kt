package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafter.PURE_ESSENCE
import org.powbot.krulvis.runecrafter.Runecrafter

class Craft(script: Runecrafter) : Leaf<Runecrafter>(script, "Craft runes") {
    override fun execute() {
        if (script.profile.type.altar.distance() >= 15) {
            walk(script.profile.type.altar)
        } else {
            val altar = Objects.stream(30).name("Altar").findFirst()
            altar.ifPresent {
                if (walkAndInteract(it, "Craft")) {
                    waitFor { !Inventory.containsOneOf(PURE_ESSENCE) }
                    sleep(1000, 1500)
                }
            }
        }
    }
}