package org.powbot.krulvis.chompy

import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.script.painter.ATPaint

class ChompyBirdPainter(script: ChompyBird) : ATPaint<ChompyBird>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.build()
    }

}