package org.powbot.krulvis.runecrafter

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics

class RunecrafterPainter(script: Runecrafter) : ATPaint<Runecrafter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.trackSkill(Skill.Runecrafting)
        paintBuilder.trackInventoryItem(script.profile.type.rune)
        return paintBuilder.build()
    }
}