package org.powbot.krulvis.dagannothkings.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.dagannothkings.DagannothKings

class Fight(script: DagannothKings) : Leaf<DagannothKings>(script, "Fighting") {
	override fun execute() {
		waitFor { script.target.dead() }
	}

}