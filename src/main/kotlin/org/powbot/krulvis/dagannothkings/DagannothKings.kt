package org.powbot.krulvis.dagannothkings

import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint

class DagannothKings : ATScript(){
	override fun createPainter(): ATPaint<*> = DKPaint(this)

	override val rootComponent: TreeComponent<*>
		get() = TODO("Not yet implemented")
}