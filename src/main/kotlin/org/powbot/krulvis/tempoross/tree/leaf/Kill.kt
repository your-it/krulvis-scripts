package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Chat
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tempoross.Data.KILLING_ANIM
import org.powbot.krulvis.tempoross.Tempoross
import kotlin.math.roundToInt

class Kill(script: Tempoross) : Leaf<Tempoross>(script, "Killing") {

	override fun execute() {
		val spirit = script.getBossPool()
		if (!spirit.valid()) {
			script.logger.info("Killing but bosspool is not valid...")
			if (script.side.bossWalkLocation.distance() > 2) {
				script.logger.info("Walking to bosspool...")
				script.walkWhileDousing(script.side.bossWalkLocation, false)
			}
			return
		}
		val killing = me.animation() != -1 && spirit.distance().roundToInt() <= 3

		Chat.canContinue()
		if (!killing && script.interactWhileDousing(spirit, "Harpoon", script.side.bossWalkLocation, true)) {
			waitForDistance(spirit, long()) { me.animation() == KILLING_ANIM }
		}
	}
}
