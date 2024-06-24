package org.powbot.krulvis.mole

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class GMPainter(script: GiantMole) : ATPaint<GiantMole>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder
			.trackSkill(Skill.Attack)
			.trackSkill(Skill.Strength)
			.trackSkill(Skill.Defence)
			.trackInventoryItems()
			.build()
	}
}