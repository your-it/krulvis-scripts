package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Random
import org.powbot.api.rt4.Camera
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.moving
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross

class Fish(script: Tempoross) : Leaf<Tempoross>(script, "Fishing") {

	override fun execute() {
		val fishSpot = script.bestFishSpot ?: return
		val interacting = me.interacting()
		val currentSpot = if (interacting is Npc) interacting else null

		if (currentSpot?.name() == "Fishing spot") {
			if (script.burningTiles.contains(me.tile())) {
				script.logger.info("Moving to save fish spot!")
				fishAtSpot(fishSpot)
			} else if (currentSpot.id() != DOUBLE_FISH_ID && fishSpot.id() == DOUBLE_FISH_ID) {
				script.logger.info("Moving to double fish spot!")
				fishAtSpot(fishSpot)
			} else {
				val tetherPole = script.getTetherPole()
				if (tetherPole.valid() && !tetherPole.inViewport()) {
					if (script.side.oddFishingSpot.distance() <= 1) {
						script.logger.info("Fishing at weird spot so using unique camera rotation")
						Camera.pitch(Random.nextInt(1200, 1300))
					} else {
						Camera.turnTo(tetherPole)
					}
				}
			}
		} else {
			script.logger.info("Fishing at first spot")
			fishAtSpot(fishSpot)
		}
	}

	private fun fishAtSpot(spot: Npc) {
		if (me.animation() == FILLING_ANIM) {
			script.logger.info("Currently in filling animation, walking first to cancel action")
			Movement.step(spot)
		}
		if (walkAndInteract(spot, "Harpoon")) {
			waitFor(Random.nextInt(1000, 5000)) {
				me.interacting().name() == "Fishing spot" || script.isWaveActive() || spot.hasLeftUs()
			}
		} else if (Movement.moving()) {
			waitFor(long()) { spot.distance() <= 2 || spot.hasLeftUs() }
		}
	}

	private fun Npc.hasLeftUs(): Boolean = Npcs.stream().at(tile()).name(name).isEmpty()

}
