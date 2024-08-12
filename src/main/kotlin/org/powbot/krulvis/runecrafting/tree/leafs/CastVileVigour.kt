package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.ouraniaPathToAltar
import kotlin.math.max

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
			if (!arcComp.visible()) {
				if (Magic.LunarSpell.SPELL_BOOK_SWAP.cast()) {
					Utils.waitFor(2500) { arceuusComp().visible() }
				}
			}
			if (arcComp.refresh().visible()) {
				arcComp.click()
				Utils.waitFor(3000) { Magic.book() == Magic.Book.ARCEUUS }
			}
		}
		val prayerPoints = Prayer.prayerPoints()
		if (Magic.book() == Magic.Book.ARCEUUS && Magic.ArceuusSpell.VILE_VIGOUR.cast()) {
			Utils.waitFor(2500) { Skills.level(Skill.Prayer) <= max(prayerPoints - 10, 0) }
		}
	}


	private fun arceuusComp() = Components.stream(219, 1).text("Arceuus").first()

}