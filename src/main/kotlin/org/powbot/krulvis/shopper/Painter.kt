package org.powbot.krulvis.shopper

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class Painter(script: Shopper): ATPaint<Shopper>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.build()
    }
}