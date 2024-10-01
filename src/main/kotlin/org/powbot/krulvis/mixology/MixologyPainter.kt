package org.powbot.krulvis.mixology

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class MixologyPainter(script: Mixology) : ATPaint<Mixology>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.addString("MixToMake") { script.mixToMake.first.name.replace("_", " ") }
			.addString("Modifier") { script.mixToMake.second.toString() }
			.addString("Gained") { script.gained.joinToString { perHourText(it) } }
			.addString("Total Points") { script.totals.joinToString() }
			.trackSkill(Skill.Herblore).build()
	}

	override fun paintCustom(g: Rendering) {
		super.paintCustom(g)
	}
}