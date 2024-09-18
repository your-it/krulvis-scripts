package org.powbot.krulvis.gwd.saradomin

import org.powbot.api.Color
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

class SaradominPainter(script: Saradomin) : ATPaint<Saradomin>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.build()
    }

    override fun paintCustom(g: Rendering) {
        super.paintCustom(g)
        script.zilyanaTiles.forEachIndexed { i, tile ->
            tile.drawOnScreen(i.toString(), outlineColor = Color.CYAN)
        }
        me.trueTile().drawOnScreen(outlineColor = Color.BLACK)
    }
}