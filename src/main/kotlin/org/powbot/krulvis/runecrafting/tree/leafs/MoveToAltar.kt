package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.runecrafting.RuneAltar
import org.powbot.krulvis.runecrafting.Runecrafter

class MoveToAltar(script: Runecrafter) : Leaf<Runecrafter>(script, "Moving to Altar") {
    override fun execute() {
        if (script.altar == RuneAltar.ZMI) Prayer.prayer(Prayer.Effect.PROTECT_FROM_MAGIC, true)
        script.altar.pathToAltar.traverse(1)
    }
}