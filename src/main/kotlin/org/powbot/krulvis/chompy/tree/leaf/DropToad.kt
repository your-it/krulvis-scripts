package org.powbot.krulvis.chompy.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.chompy.ChompyBird
import org.powbot.util.TransientGetter2D

class DropToad(script: ChompyBird) : Leaf<ChompyBird>(script, "DropToad") {
	override fun execute() {
		val invToads = Inventory.stream().name("Bloated toad").count().toInt()
		script.placementTile = me.tile()
		if (Npcs.stream().name("Bloated toad").at(script.placementTile).isNotEmpty()) {
			script.logger.info("Can't place on current tile, walking away")
			val layingToads = Npcs.get({ it.name == "Bloated toad" }).map { it.tile() }
			val flags = Movement.collisionMap(me.floor()).flags()
			script.placementTile = findStartingPointWithEmptySlots(layingToads, flags, invToads)
		}

		if (script.placementTile.distance() > 0 && Movement.step(script.placementTile, 0)) {
			waitForDistance(script.placementTile) { me.tile() == script.placementTile }
		}

		val placementTimer = Timer(2000 * invToads)
		var toad = getToad()
		script.logger.info("Found toad = ${toad.valid()}")
		while (!placementTimer.isFinished() && toad.valid()) {
			val tile = me.tile()
			script.logger.info("Dropping toad = ${toad}")
			if (toad.interact("Drop")) {
				waitFor(1200) { me.tile() != tile && !Inventory.itemAt(toad.inventoryIndex).valid() }
				sleep(250)
			}
			toad = getToad()
		}

	}

	private fun getToad() = Inventory.stream().name("Bloated toad").first()

	private fun Tile.canPlace(toads: List<Tile>, flags: TransientGetter2D<Int>): Boolean {
		return toads.none { it == this } && !blocked(flags)
	}

	private fun findStartingPointWithEmptySlots(toads: List<Tile>, flags: TransientGetter2D<Int>, requiredEmptyTiles: Int = 3): Tile {
		val directions = listOf(
			Pair(0, 1), Pair(1, 0), Pair(0, -1), Pair(-1, 0)
		) // Right, Down, Left, Up
		var steps = 1
		var tile = me.tile() //center
		if (tile.isConsecutiveEmpty(toads, flags, requiredEmptyTiles)) return tile

		while (steps < 10) {
			for (dir in directions) {
				for (i in 0 until steps) {
					tile = tile.derive(dir.first, dir.second)
					if (tile.isConsecutiveEmpty(toads, flags, requiredEmptyTiles)) return tile
				}
				// Alternate step increments between vertical and horizontal moves
				if (dir == Pair(0, 1) || dir == Pair(0, -1)) {
					steps++
				}
			}
		}
		return Tile.Nil
	}

	fun Tile.isConsecutiveEmpty(toads: List<Tile>, flags: TransientGetter2D<Int>, length: Int): Boolean {
		for (i in 0 until length) {
			if (!derive(-i, 0).canPlace(toads, flags)) {
				return false
			}
		}
		return true
	}
}