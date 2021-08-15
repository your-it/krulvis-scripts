package org.powbot.krulvis.runecrafter.tree.branch

import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafter.Runecrafter
import org.powbot.krulvis.runecrafter.tree.leaf.Craft
import org.powbot.krulvis.runecrafter.tree.leaf.EnterRuins

class AtAltar(script: Runecrafter) : Branch<Runecrafter>(script, "At Altar") {
    override val successComponent: TreeComponent<Runecrafter> = Craft(script)
    override val failedComponent: TreeComponent<Runecrafter> = AtRuins(script)

    override fun validate(): Boolean {
        return script.atAltar()
    }
}

class AtRuins(script: Runecrafter) : Branch<Runecrafter>(script, "At Ruins") {
    override val successComponent: TreeComponent<Runecrafter> = EnterRuins(script)
    override val failedComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Walking to ruins") {
        walk(script.profile.type.ruins)
    }

    override fun validate(): Boolean {
        return Objects.stream(25).name("Mysterious ruins").isNotEmpty()
    }
}
