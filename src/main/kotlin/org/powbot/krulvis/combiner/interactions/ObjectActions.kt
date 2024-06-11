package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects

object ObjectActions {

	fun GameObjectActionEvent.getObject(): GameObject? {
		return if (interaction == "Use") {
			Objects.stream().name(name).nearest().firstOrNull()
		} else {
			Objects.stream().name(name).action(interaction).nearest().firstOrNull()
		}
	}

}