package org.powbot.krulvis.runecrafting.tree.leafs.abyss

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.Abyss

class EnterPortal(script: Runecrafter) : Leaf<Runecrafter>(script, "Entering Portal") {
    override fun execute() {
        val portal = Abyss.getPortal(script.altar.name)
        if (portal.valid()) {
            if (walkAndInteract(portal, "Exit-through")) {
                waitForDistance(portal) { script.altar.getAltar() != null }
            }
        } else {
            script.logger.info("Cannot find abyss portal")
        }
    }

}