package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.fullPrayer
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.ouraniaPrayerAltarPath
import org.powbot.krulvis.runecrafting.tree.leafs.CastVileVigour

class ShouldPrayAtAltar(script: Runecrafter) : Branch<Runecrafter>(script, "Should pray at Chaos Altar") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldCastVileVigour(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Pray at altar") {
        val altar = script.findChaosAltar()
        if (altar.distance() > 4 || !altar.inViewport()) {
            ouraniaPrayerAltarPath.traverse(1, distanceToLastTile = 4) {
                if (altar.inViewport() && altar.interact("Pray-at")) {
                    waitForDistance(altar) { fullPrayer() }
                }
            }
        } else if (walkAndInteract(altar, "Pray-at")) {
            waitForDistance(altar) { fullPrayer() }
        }
    }


    override fun validate(): Boolean {
        if (fullPrayer()) return false
        script.chaosAltar = script.findChaosAltar()
        return script.chaosAltar.valid()
    }
}


class ShouldCastVileVigour(script: Runecrafter) : Branch<Runecrafter>(script, "Should cast Vile Vigour") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldSwitchBackSpellBook(script)
    override val successComponent: TreeComponent<Runecrafter> = CastVileVigour(script)

    override fun validate(): Boolean {
        if (Movement.energyLevel() > 50) return false
        return script.vileVigour && fullPrayer() && script.findChaosAltar()
            .valid() && Magic.ArceuusSpell.VILE_VIGOUR.canCast()
    }
}

class ShouldSwitchBackSpellBook(script: Runecrafter) : Branch<Runecrafter>(script, "ShouldSwitchSpellBook") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldBank(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "SwitchingSpellBook") {
        val name = if (script.spellBook == Magic.Book.MODERN) "Standard" else script.spellBook!!.name.lowercase()
        if (walkAndInteract(altar, name)) {
            waitForDistance(altar) { script.spellBook == Magic.book() }
        }
    }

    var altar = GameObject.Nil

    override fun validate(): Boolean {
        if (script.spellBook != null && script.spellBook != Magic.book()) {
            altar = script.getSpellBookAltar()
            return altar.valid()
        }
        return false
    }
}