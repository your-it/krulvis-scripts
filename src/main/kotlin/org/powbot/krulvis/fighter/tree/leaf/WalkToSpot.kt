package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class WalkToSpot(script: Fighter) : Leaf<Fighter>(script, "Walking to spot") {
	override fun execute() {
		Chat.clickContinue()
		val npcTeleport = script.npcTeleport
		val spot = script.centerTile()
		val nearby = script.nearbyMonsters()
		if (nearby.none { it.reachable() } || (spot.distance() > if (script.useSafespot) 0 else script.radius)) {
			val path = LocalPathFinder.findPath(spot)
			if (path.isNotEmpty()) {
				script.npcTeleport.executed = false
				path.traverseUntilReached(0.0)
			} else if (npcTeleport.execute()) {
				script.aggressionTimer.reset()
				Movement.walkTo(spot)
			}
		}
	}
}