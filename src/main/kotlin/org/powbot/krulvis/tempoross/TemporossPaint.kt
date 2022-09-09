package org.powbot.krulvis.tempoross

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.drawing.Rendering

class TemporossPaint(script: Tempoross) : ATPaint<Tempoross>(script, 110, 210) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder.addString("Reward credits:") { "${script.rewardGained}, ${script.timer.getPerHour(script.rewardGained)}/hr" }
        paintBuilder.addString("Points obtained:") { "${if (script.rounds > 0) script.pointsObtained / script.rounds else "-"}/round" }
        paintBuilder.trackSkill(Skill.Fishing)
        return paintBuilder.build()
    }

    override fun paintCustom(g: Rendering) {
        if (debugComponents) {
            val blockedTiles = script.burningTiles.toList()
            val paths = script.triedPaths.toList()
            blockedTiles.forEach {
                val t = it
                if (t != Tile.Nil) {
                    it.drawOnScreen(null, Color.RED)
                }
            }
            if (blockedTiles.isNotEmpty() && paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val dangerous = tiles.any { script.burningTiles.contains(it) }
                    tiles.forEach { tile ->
                        tile.drawOnScreen(
                            null,
                            if (blockedTiles.contains(tile)) Color.BLACK else if (dangerous) Color.ORANGE else Color.GREEN
                        )
                    }
                }
            }
        }
    }

}