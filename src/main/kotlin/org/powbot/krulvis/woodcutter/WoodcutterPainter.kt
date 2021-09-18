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
            .trackInventoryItems(
                1511, 1513, 1515, 1517, 1519, 1521, 2862, 6332, 6333, 19669,
                5070, 5071, 5072, 5073, 5074
            )
            .build()
    }

    override fun paintCustom(g: Graphics) {
        if (script.lastLeaf is Burn)
            script.burnTile?.drawOnScreen(g)
    }
}