package org.powbot.krulvis.runecrafter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafter.Runecrafter

class OpenBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Open bank") {
    override fun execute() {
        val portal = portal()
        val b = Bank.nearest()
        if (b.tile() != Tile.Nil) {
            script.log.info("Bank is present at=${b.tile()}")
            Bank.open()
        } else if (portal != null) {
            script.log.info("Gotta go away from altar first")
            if (walkAndInteract(portal, "Use")) {
                waitFor { !script.atAltar() }
            }
        } else {
            script.log.info("Walking to bank")
            walk(script.profile.type.bank)
        }
    }

    fun portal(): GameObject? = Objects.stream(25).name("Portal").nearest().firstOrNull()
}