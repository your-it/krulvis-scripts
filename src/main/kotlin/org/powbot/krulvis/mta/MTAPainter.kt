package org.powbot.krulvis.mta

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class MTAPainter(script: MTA) : ATPaint<MTA>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackSkill(Skill.Magic)
            .addString("Points") { "${script.gainedPoints}, ${script.timer.getPerHour(script.gainedPoints)}/hr" }
            .build()
    }

//    override fun paintCustom(g: Rendering) {
//        if (!TelekineticRoom.shouldInstantiate()) {
//            TelekineticRoom.paint()
//        }
//    }
}