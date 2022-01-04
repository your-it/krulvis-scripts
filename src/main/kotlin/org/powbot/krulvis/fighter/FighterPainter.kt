package org.powbot.krulvis.fighter

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics

class FighterPainter(script: Fighter) : ATPaint<Fighter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder
            .trackSkill(Skill.Attack)
            .trackSkill(Skill.Strength)
            .trackSkill(Skill.Defence)
            .trackSkill(Skill.Hitpoints)
            .trackSkill(Skill.Prayer)
            .trackSkill(Skill.Magic)
            .trackSkill(Skill.Ranged)
            .trackSkill(Skill.Slayer)
            .addCheckbox("Stop after Slay task", "stopAfterTask", false)
            .withTotalLoot(true)
        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
        val target = script.currentTarget
        target?.draw(g)
        if (target != null) {
            g.drawString("HP: ${target.healthPercent()}", 500, 200)
        }
//        script.currentTarget?.tile()?.drawOnScreen(g)
    }
}