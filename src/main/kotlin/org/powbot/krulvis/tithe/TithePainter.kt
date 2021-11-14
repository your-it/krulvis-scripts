package org.powbot.krulvis.tithe

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class TithePainter(script: TitheFarmer) : ATPaint<TitheFarmer>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Gained points:") { "${script.gainedPoints}, (${script.timer.getPerHour(script.gainedPoints)}/hr)" }
            .trackSkill(Skill.Farming)
            .addCheckbox("Last round:", "lastRound", false)
            .build()
    }
}