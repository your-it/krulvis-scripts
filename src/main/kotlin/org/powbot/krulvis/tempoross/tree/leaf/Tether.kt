package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbors
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tempoross.Tempoross


class Tether(script: Tempoross) : Leaf<Tempoross>(script, "Tethering") {
	override fun execute() {
		if (script.isTethering()) {
			script.logger.info("Waiting for wave to pass..")
			if (waitForWave()) {
				script.logger.info("Done tethering...")
			}
			return
		}
		val pole = script.getTetherPole()

		val poleTiles = pole.tile().getWalkableNeighbors(diagonalTiles = true)
		val nearestTile = poleTiles.minByOrNull { it.distance() } ?: return
		val safeTile = poleTiles.filterNot { script.burningTiles.contains(it) }.minByOrNull { it.distance() }
		if (safeTile == nearestTile) {
			if (pole.actions().contains("Repair") && script.hasHammer()) {
				if (walkAndInteract(pole, "Repair")) {
					waitForDistance(pole, long()) {
						!script.isWaveActive() || !script.getTetherPole().actions().contains("Repair")
					}
				}
			}
			if (walkAndInteract(pole, "Tether")) {
				waitFor(2500 + 350 * pole.distance().toInt()) { !script.isWaveActive() || script.isTethering() }
			}
		} else {
			script.walkWhileDousing(nearestTile, true)
		}
	}

	private fun waitForWave(): Boolean {
		return waitFor(long()) { !script.isWaveActive() }
	}

}