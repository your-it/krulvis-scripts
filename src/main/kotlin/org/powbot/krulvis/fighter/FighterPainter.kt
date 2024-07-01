package org.powbot.krulvis.fighter

import org.powbot.api.Color
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.paint.TextPaintItem
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.slayer.Slayer
import org.powbot.mobile.drawing.Rendering

class FighterPainter(script: Fighter) : ATPaint<Fighter>(script) {

	val slayerTracker = listOf(TextPaintItem { "Monsters left:" }, TextPaintItem { Slayer.taskRemainder().toString() })
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		paintBuilder.addString("Target") {
			"Name=${TargetWidget.name()}, HP=${TargetWidget.health()}, ${script.currentTarget?.healthPercent()}%"
		}
			.addCheckbox("Stop after Slay task", "stopAfterTask", false)
			.withTotalLoot(true)
			.addString("Npc Death Watchers") {
				script.npcDeathWatchers.joinToString { "${it.npc.name}: ${it.active}" }
			}
			.trackSkill(Skill.Attack)
			.trackSkill(Skill.Strength)
			.trackSkill(Skill.Defence)
			.trackSkill(Skill.Hitpoints)
			.trackSkill(Skill.Prayer)
			.trackSkill(Skill.Magic)
			.trackSkill(Skill.Ranged)
			.trackSkill(Skill.Slayer)

		return paintBuilder.build()
	}

	override fun paintCustom(g: Rendering) {
		val target = script.currentTarget
		if (target != null) {
			g.drawString("Vis  : ${target.healthBarVisible()}", 500, 200)
			g.drawString("HP   : ${target.healthPercent()}", 500, 220)
			g.drawString("Anim : ${target.animation()}", 500, 240)
			g.drawString("Valid: ${target.valid()}", 500, 260)
			g.drawString(
				"Watcher count total=${script.npcDeathWatchers.size}, target=${script.npcDeathWatchers.count { it.npc == target }}",
				500,
				300
			)
		}
		val lootWatcher = script.lootWachter ?: return
		if (lootWatcher.active)
			lootWatcher.tile.drawOnScreen(outlineColor = Color.CYAN)
	}
}