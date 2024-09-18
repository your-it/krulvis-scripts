package org.powbot.krulvis.gwd.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.gwd.GWD.GOD_WARS_AREA
import org.powbot.krulvis.gwd.GWDScript

class ToGWD<S>(script: S) : Leaf<S>(script, "Getting KC") where S : GWDScript<S> {
	private val entranceTile = Tile(0, 0, 0)
	override fun execute() {
		if (script.gwdTeleport.execute()) {
			val entrance = Objects.stream(entranceTile).name("Entrance").first()
			if (entrance.valid()) {
				if (walkAndInteract(entrance, "Enter")) {
					waitForDistance(entrance) { GOD_WARS_AREA.contains(me) }
				}
			} else {
				Movement.walkTo(entranceTile)
			}
		}
	}
}