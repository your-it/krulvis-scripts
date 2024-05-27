package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.telekenesis.TelekineticRoom

class MoveToTelekineticPosition(script: MTA) : Leaf<MTA>(script, "Go to casting position") {

	override fun execute() {
		val optimal = TelekineticRoom.optimal()
		if (optimal.distanceTo(Movement.destination()) > 0) {
			script.log.info("Moving to position")
			val startTime = System.currentTimeMillis()
			val matrix = optimal.matrix()
			if (!matrix.inViewport()) {
				Movement.step(optimal)
			} else {
				matrix.click()
				if (waitFor(1000) { Movement.destination() == optimal }) {
					waitForDistance(optimal) { optimal == me.tile() }
				}
			}
			val duration = System.currentTimeMillis() - startTime
			script.log.info("Done moving to position in $duration ms")
		}
	}

}