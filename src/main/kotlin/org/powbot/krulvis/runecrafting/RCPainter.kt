package org.powbot.krulvis.runecrafting

import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class RCPainter(script: Runecrafter) : ATPaint<Runecrafter>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder.trackSkill(Skill.Runecrafting)
			.trackInventoryItemQ(Rune.SOUL.id)
			.trackInventoryItemQ(Rune.LAW.id)
			.trackInventoryItemQ(Rune.BLOOD.id)
			.trackInventoryItemQ(Rune.DEATH.id)
			.trackInventoryItemQ(Rune.NATURE.id)
			.trackInventoryItemQ(Rune.ASTRAL.id)
			.trackInventoryItemQ(Rune.COSMIC.id)
			.trackInventoryItemQ(Rune.CHAOS.id)

		return paintBuilder.build()
	}
}