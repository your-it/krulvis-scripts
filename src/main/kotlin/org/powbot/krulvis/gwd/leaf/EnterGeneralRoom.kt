package org.powbot.krulvis.gwd.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.gwd.GWD
import org.powbot.krulvis.gwd.GWDScript

class EnterGeneralRoom<S>(script: S) : Leaf<S>(script, "Enter General Room") where S : GWDScript<S> {
	override fun execute() {
		val door = Objects.stream(script.god.doorTile, GameObject.Type.INTERACTIVE).action("Open (normal)").first()
		if (true || walkAndInteract(door, "Open")) {
			waitForDistance(door) { GWD.nearAltar() }
		}
	}
}