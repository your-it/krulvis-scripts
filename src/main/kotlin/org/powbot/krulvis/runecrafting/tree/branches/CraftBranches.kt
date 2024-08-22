package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.runecrafting.ABYSS
import org.powbot.krulvis.runecrafting.RuneAltar
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.leafs.CraftRunes
import org.powbot.krulvis.runecrafting.tree.leafs.MoveToAltar

class AtAltar(script: Runecrafter) : Branch<Runecrafter>(script, "At altar?") {
    override val failedComponent: TreeComponent<Runecrafter> = AbyssMethod(script)
    override val successComponent: TreeComponent<Runecrafter> = CraftRunes(script)

    override fun validate(): Boolean {
        script.bankTeleport.executed = false
        script.logger.info("AtAltar")
        return script.altar.atAltar()
    }
}

class AbyssMethod(script: Runecrafter) : Branch<Runecrafter>(script, "Doing abyss?") {
    override val failedComponent: TreeComponent<Runecrafter> = BloodMethod(script)
    override val successComponent: TreeComponent<Runecrafter> = InInnerCircle(script)

    override fun validate(): Boolean {
        return script.method == ABYSS
    }
}

//class ShouldTeleport(script: Runecrafter) : Branch<Runecrafter>(script, "ShouldTeleport?") {
//    override val failedComponent: TreeComponent<Runecrafter> = BloodMethod(script)
//    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "TeleportToAltar") {
//        script.altarTeleport.execute()
//    }
//
//    override fun validate(): Boolean {
//        if (script.altarTeleport.teleport == null) script.altarTeleport.executed = true
//        return !script.altarTeleport.executed
//    }
//}

class BloodMethod(script: Runecrafter) : Branch<Runecrafter>(script, "IsBloodCrafting?") {
    override val failedComponent: TreeComponent<Runecrafter> = MoveToAltar(script)
    override val successComponent: TreeComponent<Runecrafter> = InRuinsArea(script)

    override fun validate(): Boolean {
        return script.altar == RuneAltar.BLOOD
    }
}


