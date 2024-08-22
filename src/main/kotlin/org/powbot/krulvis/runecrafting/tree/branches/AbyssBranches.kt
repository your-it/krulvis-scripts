package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.Abyss
import org.powbot.krulvis.runecrafting.tree.leafs.abyss.EnterAbyss
import org.powbot.krulvis.runecrafting.tree.leafs.abyss.EnterInnerPortal
import org.powbot.krulvis.runecrafting.tree.leafs.abyss.EnterPortal
import org.powbot.krulvis.runecrafting.tree.leafs.abyss.RepairPouchInAbyss

class InInnerCircle(script: Runecrafter) : Branch<Runecrafter>(script, "In inner circle?") {
    override val failedComponent: TreeComponent<Runecrafter> = InOuterCircle(script)
    override val successComponent: TreeComponent<Runecrafter> = ShouldRepairDarkMage(script)

    override fun validate(): Boolean {
        return Abyss.inInnerCircle()
    }
}

class ShouldRepairDarkMage(script: Runecrafter) : Branch<Runecrafter>(script, "Should talk to dark mage?") {
    override val failedComponent: TreeComponent<Runecrafter> = EnterPortal(script)
    override val successComponent: TreeComponent<Runecrafter> = RepairPouchInAbyss(script)

    override fun validate(): Boolean {
        return EssencePouch.inInventory().any { it.shouldRepair() }
    }
}

class InOuterCircle(script: Runecrafter) : Branch<Runecrafter>(script, "In outer circle?") {
    override val failedComponent: TreeComponent<Runecrafter> = EnterAbyss(script)
    override val successComponent: TreeComponent<Runecrafter> = EnterInnerPortal(script)

    override fun validate(): Boolean {
        return Abyss.inOuterCircle()
    }
}