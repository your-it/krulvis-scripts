package org.powbot.krulvis.woodcutter

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.woodcutter.tree.leaf.Burn
import org.powbot.mobile.drawing.Graphics

class WoodcutterPainter(script: Woodcutter) : ATPaint<Woodcutter>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackSkill(Skill.Woodcutting)
            .trackSkill(Skill.Firemaking)
            .trackInventoryItems(
                *script.LOGS, *script.NESTS
            )
            .build()
    }

    override fun paintCustom(g: Graphics) {
        if (script.lastLeaf is Burn)
            script.burnTile?.drawOnScreen(g)
    }
}