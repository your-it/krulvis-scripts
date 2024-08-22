package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tempoross.Data.WATER_ANIM
import org.powbot.krulvis.tempoross.Tempoross

class FillBuckets(script: Tempoross) : Leaf<Tempoross>(script, "Getting water") {
	override fun execute() {
		val humidify = Magic.LunarSpell.HUMIDIFY
		if (humidify.canCast()) {
			if (humidify.cast()) {
				waitFor(1500) { !Inventory.containsOneOf(EMPTY_BUCKET) }
			}
		} else if (me.animation() != WATER_ANIM) {
			val waterPump = script.getWaterpump()
			val action = if (waterPump.actions().contains("Use")) "Use" else "Fill-bucket"
			if (walkAndInteract(waterPump, action) && waitForDistance(waterPump, long()) { me.animation() == WATER_ANIM }
			) {
				waitFor(long()) { !Inventory.containsOneOf(EMPTY_BUCKET) }
			}
		}
	}


}