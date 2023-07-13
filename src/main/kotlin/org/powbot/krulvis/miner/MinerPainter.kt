package org.powbot.krulvis.miner

import org.powbot.api.Color
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.miner.tree.leaf.WalkToSpot
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.drawing.Rendering


class MinerPainter(script: Miner) : ATPaint<Miner>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder
                .trackSkill(Skill.Mining)
                .trackInventoryItems(
                        12012,
                        *Ore.values().filter { it != Ore.PAY_DIRT }.flatMap { it.ids.toList() }.toIntArray()
                )
                .withTotalLoot(true)
        return paintBuilder.build()
    }
}