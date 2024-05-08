package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.leafs.GetNPCContactRunes
import org.powbot.krulvis.runecrafting.tree.leafs.RepairPouches

class ShouldRepair(script: Runecrafter) : Branch<Runecrafter>(script, "Should repair?") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldPrayAtAltar(script)
    override val successComponent: TreeComponent<Runecrafter> = HasRunes(script)

    override fun validate(): Boolean {
        return EssencePouch.inInventory().any { it.shouldRepair() }
    }
}

class HasRunes(script: Runecrafter) : Branch<Runecrafter>(script, "Has runes to repair?") {
    override val failedComponent: TreeComponent<Runecrafter> = GetNPCContactRunes(script)
    override val successComponent: TreeComponent<Runecrafter> = RepairPouches(script)

    override fun validate(): Boolean {
        return Magic.LunarSpell.NPC_CONTACT.canCast()
    }
}