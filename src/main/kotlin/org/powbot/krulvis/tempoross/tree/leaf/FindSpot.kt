package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross

class FindSpot(script: Tempoross) : Leaf<Tempoross>(script, "FindSpotToFish") {

	override fun execute() {
		script.logger.info("No safe fishing spot found!")
		if (script.burningTiles.contains(me.tile())) {
			val safeTile = findSaveTile(me.tile())
			script.logger.info("We are standing on a dangerous tile! Walking to safeTile=$safeTile")
			if (safeTile != null && Movement.step(safeTile)) {
				waitFor { me.tile() == safeTile }
			}
		} else if (script.fishSpots.any { it.second.actions.last().destination.distance() <= 1 }) {
			script.logger.info("Nearby blocked fishing spot found that is blocked")
			val blockedTile =
				script.fishSpots.first { it.second.actions.last().destination.distance() <= 1 }.second.actions.last()
			val fireOptional =
				Npcs.stream().name("Fire").within(blockedTile.destination, 2.0).nearest().findFirst()
			if (fireOptional.isPresent) {
				script.logger.info("Dousing nearby fire...")
				val fire = fireOptional.get()
				if (walkAndInteract(fire, "Douse")) {
					waitFor { Npcs.stream().at(fire.tile()).name("Fire").isEmpty() }
				}
			}
		} else {
			script.logger.info("No fishing spot found, walking to Totem pole / anchor")
			var path = LocalPathFinder.findPath(script.side.totemLocation)
			if (path.isEmpty()) {
				path = LocalPathFinder.findPath(script.side.anchorLocation)
			}
			script.walkWhileDousing(path, false)
		}
	}

	private fun findSaveTile(tile: Tile): Tile? {
		return tile.getWalkableNeighbor(allowSelf = false, checkForWalls = false, diagonalTiles = true) {
			!script.burningTiles.contains(it)
		}
	}

}
