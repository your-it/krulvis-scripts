package org.powbot.krulvis.miner

import org.powbot.api.Condition.sleep
import org.powbot.api.Tile
import org.powbot.api.rt4.Objects
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(
    name = "krul ColorFinder",
    description = "Helps find colors of non-working miner rocks",
    author = "Krulvis",
    version = "1.0.0",
    markdownFileName = "Miner.md",
    category = ScriptCategory.Mining
)
class ColorFinder : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return Painter(this)
    }

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "Chill") {
        Objects.stream(50).filtered { it.originalColors().isNotEmpty() }.forEach {
            log.info("Found obj with colors: ${it.name}, ${it.originalColors().joinToString()}")
        }
        sleep(1000)
    }
}

class Painter(script: ColorFinder) : ATPaint<ColorFinder>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.build()
    }

    override fun paintCustom(g: Rendering) {
        val myTile = me.tile()
        val tiles = listOf(
            Tile(myTile.x - 1, myTile.y),
            Tile(myTile.x - 1, myTile.y + 1),
            Tile(myTile.x - 1, myTile.y - 1),
            Tile(myTile.x, myTile.y + 1),
            Tile(myTile.x, myTile.y - 1),
            Tile(myTile.x + 1, myTile.y + 1),
            Tile(myTile.x + 1, myTile.y),
            Tile(myTile.x + 1, myTile.y - 1),
        )
        tiles.forEach { tile ->
            val gos = Objects.stream().at(tile).filtered { it.name.isNotEmpty() }
            gos.forEach { script.log.info("GO AT TILE=${tile}: ${it.name}, cols=${it.modifiedColors().joinToString()}") }
            val colors = gos.firstOrNull { it.modifiedColors().isNotEmpty() }?.modifiedColors()
            if (colors != null) {
                tile.drawOnScreen(colors.joinToString("\n"))
            }
        }
    }
}

fun main() {
    ColorFinder().startScript()
}