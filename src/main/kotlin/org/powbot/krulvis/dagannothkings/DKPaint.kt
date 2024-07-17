package org.powbot.krulvis.dagannothkings

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class DKPaint(script: DagannothKings) : ATPaint<DagannothKings>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.build()
	}
}