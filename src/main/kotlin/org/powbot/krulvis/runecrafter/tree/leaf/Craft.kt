package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafter.PURE_ESSENCE
import org.powbot.krulvis.runecrafter.Runecrafter

class Craft(script: Runecrafter) : Leaf<Runecrafter>(script, "Craft runes") {
    override fun execute() {
        if (script.profile.type.altar.distance() >= 15) {
            walk(script.profile.type.altar)
        } else {
            val altar = ctx.objects.toStream(30).name("Altar").findFirst()
            altar.ifPresent {
                if (interact(it, "Craft")) {
                    waitFor { !ctx.inventory.containsOneOf(PURE_ESSENCE) }
                }
            }
        }
    }
}