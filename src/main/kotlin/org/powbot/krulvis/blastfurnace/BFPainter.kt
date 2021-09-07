package org.powbot.krulvis.blastfurnace

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics

class BFPainter(script: BlastFurnace) : ATPaint<BlastFurnace>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.addString("Bars:") { script.bar.blastFurnaceCount.toString() }
        paintBuilder.addString(
            { "${script.bar.primary.name} Ore: " },
            { script.bar.primary.blastFurnaceCount.toString() })
        if (script.bar.secondaryMultiplier > 0) {
            paintBuilder.addString(
                { "${script.bar.secondary.name} Ore: " },
                { script.bar.secondary.blastFurnaceCount.toString() })
        }
        paintBuilder.trackSkill(Skill.Smithing)
        paintBuilder.trackInventoryItem(script.bar.id)

        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
    }
}