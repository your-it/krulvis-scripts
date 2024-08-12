package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.ouraniaPathToAltar

class CastVileVigour(script: Runecrafter) : Leaf<Runecrafter>(script, "CastVileVigour") {
	override fun execute() {
		val altar = script.chaosAltar
		if (altar.distance() > 3) {
			ouraniaPathToAltar.traverse(1, distanceToLastTile = 4) {
				if (Movement.energyLevel() < 50) {
					castVileVigour()
				}
			}
		} else {
			castVileVigour()
		}
	}

	private fun castVileVigour() {
		if (Magic.book() != Magic.Book.ARCEUUS) {
			val arcComp = arceuusComp()
			script.logger.info("ARCEUUS spellbook not valid, comp=${arcComp}")
			if (arcComp.visible()) {
				arcComp.click()
				Utils.waitFor(3000) { Magic.book() == Magic.Book.ARCEUUS }
			}
			if (Magic.book() == Magic.Book.ARCEUUS && Magic.LunarSpell.SPELL_BOOK_SWAP.cast()) {
				Utils.waitFor(2500) { arceuusComp().visible() }
			}
		} else if (Magic.ArceuusSpell.VILE_VIGOUR.cast()) {
			Utils.waitFor(2500) { Skills.level(Skill.Prayer) == 0 || Movement.energyLevel() > 50 }
		}
	}


	private fun arceuusComp() = Components.stream(219, 1).text("Arceuus").first()

}