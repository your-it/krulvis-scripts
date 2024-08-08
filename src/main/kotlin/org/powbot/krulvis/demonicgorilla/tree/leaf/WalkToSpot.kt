package org.powbot.krulvis.demonicgorilla.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.demonicgorilla.DemonicGorilla

class WalkToSpot(script: DemonicGorilla) : Leaf<DemonicGorilla>(script, "Walking to spot") {
	override fun execute() {
		Chat.clickContinue()
		val spot = script.centerTile
		val nearby = script.nearbyMonsters()
		if (nearby.none { it.reachable() } || (spot.distance() > 15)) {
			if (spot.distance() <= 10 && spot.reachable()) {
				Movement.step(spot, 0)
				return
			}
			val path = LocalPathFinder.findPath(spot)
			if (path.isNotEmpty()) {
//				script.monsterTeleport.executed = false
				path.traverseUntilReached(0.0)
			} else if (script.npcTeleport.execute()) {
				script.aggressionTimer.reset()
				Movement.builder(spot).setWalkUntil { spot.distance() < 10 }.move()
			}
		}
	}
}