package org.powbot.krulvis.miner

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics


class MinerPainter(script: Miner) : ATPaint<Miner>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.trackSkill(Skill.Mining)
        //TODO("Add lootTracker for big list of loot")
        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
    }
}