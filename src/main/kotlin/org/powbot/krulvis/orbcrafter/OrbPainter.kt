package org.powbot.krulvis.orbcrafter

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics

class OrbPainter(script: OrbCrafter) : ATPaint<OrbCrafter>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        PaintBuilder().trackSkill(Skill.Magic).trackInventoryItems(123)
        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
    }


}