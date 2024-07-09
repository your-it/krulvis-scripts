package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.DepositBox
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distanceM
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.mobile.script.ScriptManager

class Walk(script: Woodcutter) : Leaf<Woodcutter>(script, "Walk to Trees") {
	override fun execute() {
		val locs = script.trees
		Bank.close()
		DepositBox.close()
		script.chopDelay.forceFinish()
		if (locs.isEmpty()) {
			script.logger.info("Script requires at least 1 Tree GameObject set in the Configuration")
			ScriptManager.stop()
		} else {
			val tile = locs.minByOrNull { it.distance() } ?: return
			if (script.forceWeb) {
				Movement.builder(tile).setForceWeb(true).move()
				return
			}
			if (tile.distance() < 15) {
				Movement.step(tile)
				waitFor(long()) { tile.distance() < 5 }
			} else if (tile.floor == 1 && tile.distanceM(me) <= 20 && me.floor() == 0) { //Climb ladder to access Redwood trees
				val ladder = Objects.stream().name("Rope ladder").action("Climb-up").nearest().first()
				if (walkAndInteract(ladder, "Climb-up")) {
					waitForDistance(ladder) { me.floor() == 1 }
				}
			} else {
				Movement.walkTo(tile.getStraightNeighor())
			}
		}
	}

	/**
	 * Returns: [Tile] nearest neighbor or self as which is walkable
	 */
	fun Tile.getStraightNeighor(
	): Tile? {
		return getStraightNeighors().minByOrNull { it.distance() }
	}

	fun Tile.getStraightNeighors(
	): List<Tile> {

		val t = tile()
		val x = t.x()
		val y = t.y()
		val f = t.floor()
		val cm = Movement.collisionMap(f).flags()

		val neighbors = mutableListOf<Tile>()
		for (yy in 1..2) neighbors.add(Tile(x, y + yy, f))
		for (yy in 1..2) neighbors.add(Tile(x, y - yy, f))
		for (xx in 1..2) neighbors.add(Tile(x + xx, y, f))
		for (xx in 1..2) neighbors.add(Tile(x - xx, y, f))

		return neighbors.filter { !it.blocked(cm) }
	}
}