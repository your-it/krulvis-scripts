package org.powbot.krulvis.gwd.leaf

import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.gwd.GWDScript

class WalkToKCLocation<S>(script: S) : Leaf<S>(script, "Walk to KC location") where S : GWDScript<S> {
	override fun execute() {
		Movement.walkTo(script.god.kcLocation)
	}
}