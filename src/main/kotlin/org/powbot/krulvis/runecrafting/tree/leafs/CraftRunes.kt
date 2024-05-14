package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Game
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter

class CraftRunes(script: Runecrafter) : Leaf<Runecrafter>(script, "Crafting runes") {
    override fun execute() {
        EssencePouch.inInventory().forEach { it.empty() }
        val altar = script.altar.getAltar() ?: return

        if (walkAndInteract(altar, "Craft-rune")) {
            Game.tab(Game.Tab.INVENTORY)
            waitFor { EssencePouch.essenceCount() == 0 }
        }
    }
}