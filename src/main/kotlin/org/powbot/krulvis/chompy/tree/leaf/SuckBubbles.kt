package org.powbot.krulvis.chompy.tree.leaf

import org.powbot.api.Production.stoppedMaking
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.chompy.ChompyBird

class SuckBubbles(script: ChompyBird) : Leaf<ChompyBird>(script, "SuckBubbles") {
	override fun execute() {
		val swampBubbles = Objects.stream().name("Swamp bubbles").action("Suck").nearest().first()
		if (walkAndInteract(swampBubbles, "Suck")) {
			if (waitForDistance(swampBubbles) { me.animation() != -1 }) {
				if (waitFor { !stoppedMaking(2872) }) {
					waitFor(long()) { stoppedMaking(2872) }
				}
			}
		}
	}
}