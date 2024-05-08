package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.fullPrayer
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter

class ShouldPrayAtAltar(script: Runecrafter) : Branch<Runecrafter>(script, "Should pray at altar") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldCastVileVigour(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Pray at altar") {
        if (walkAndInteract(altar, "Pray-at")) {
            waitForDistance(altar) { fullPrayer() }
        }
    }

    var altar: GameObject = GameObject.Nil

    override fun validate(): Boolean {
        if (fullPrayer()) return false
        altar = script.getAltar()
        return altar.valid()
    }
}


class ShouldCastVileVigour(script: Runecrafter) : Branch<Runecrafter>(script, "Should cast Vile Vigour") {
    override val failedComponent: TreeComponent<Runecrafter> = ShouldBank(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Cast vile vigour") {
        if (Magic.book() != Magic.Book.ARCEUUS) {
            val arcComp = arceuusComp()
            if (arcComp.visible()) {
                arcComp.click()
                waitFor(3000) { Magic.book() == Magic.Book.ARCEUUS }
            } else if (Magic.LunarSpell.SPELL_BOOK_SWAP.cast()) {
                waitFor(2500) { arceuusComp().visible() }
            }
        } else if (vileVigour.cast()) {
            waitFor(2500) { Skills.level(Skill.Prayer) == 0 || Movement.energyLevel() > 95 }
        }
    }
    private val vileVigour = Magic.ArceuusSpell.VILE_VIGOUR

    private fun arceuusComp() = Components.stream(219, 1).text("Arceuus").first()

    override fun validate(): Boolean {
        if (!script.vileVigour || !fullPrayer() || script.getAltar().distance() > 5 || !vileVigour.canCast()) return false
        return Movement.energyLevel() <= 90
    }
}