package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafter.Runecrafter

class OpenBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Open bank") {
    override fun execute() {
        if (ctx.bank.present()) {
            ctx.bank.open()
        } else if (script.atAltar()) {
            println("Gotta go away from altar first")
            ctx.objects.toStream(40).name("Portal").findFirst().ifPresent {
                if (interact(it, "Use")) {
                    waitFor { !script.atAltar() }
                }
            }
        } else {
            walk(script.profile.type.bank)
        }
    }
}