package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA

class GoInside(script: MTA, private val room: String) : Leaf<MTA>(script, "Go Inside Room") {
	override fun execute() {

		val entrance = Objects.stream(50).type(GameObject.Type.INTERACTIVE).nameContains(room).action("Enter").first()
		if (!entrance.valid()) {
			script.log.info("entrance can't be found, looking for $room")
			Objects.stream(50).type(GameObject.Type.INTERACTIVE).action("Enter").toList().forEach {
				script.log.info("Found $it")
			}
			Movement.walkTo(Tile(3363, 3317, 0))
		} else if (walkAndInteract(entrance, "Enter")) {
			waitForDistance(entrance) { inside() }
		}
	}

	private fun inside() = !Objects.stream(50).type(GameObject.Type.INTERACTIVE).nameContains(room).action("Enter").first().valid()

}