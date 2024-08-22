package org.powbot.krulvis.runecrafting.tree.leafs.abyss

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter

class RepairPouchInAbyss(script: Runecrafter) : Leaf<Runecrafter>(script, "Repair pouches in Abyss") {
    override fun execute() {
        val mage = Npcs.stream().name("Dark Mage").first()
        if (walkAndInteract(mage, "Repairs")) {
            waitForDistance(mage) { EssencePouch.inInventory().none { it.shouldRepair() } }
        }
    }

}