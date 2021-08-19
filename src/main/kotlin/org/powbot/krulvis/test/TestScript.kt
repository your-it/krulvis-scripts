package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.LocalPathFinder.isRockfall
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.selectors.GameObjectOption
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "testscript", version = "1.0d", description = "")
class TestScript : ATScript() {
    override val painter: ATPainter<*> = TestPainter(this)

    val dest = Tile(3208, 3221, 2) //Lummy top bank
    val oddRockfall = Tile(x = 3216, y = 3210, floor = 0)
    var flags = emptyArray<IntArray>()

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//        flags = Movement.collisionMap(Players.local().floor()).flags()
        WebWalking.walkTo(dest, false)
    }
}

class TestPainter(script: TestScript) : ATPainter<TestScript>(script, 10, 500) {
    override fun paint(g: Graphics, startY: Int) {
        var y = startY
        y = drawSplitText(g, "Destination loaded: ", script.dest.loaded().toString(), x, y)
        val regionTile = script.dest.regionTile()
        y = drawSplitText(g, "Destination regionTile: ", regionTile.toString(), x, y)

        script.oddRockfall.drawOnScreen(g)
        y = drawSplitText(g, "Rockfall:", script.oddRockfall.rockfallBlock(script.flags).toString(), x, y)
    }

    fun Tile.rockfallBlock(flags: Array<IntArray>): Boolean {
        return collisionFlag(flags) in intArrayOf(Flag.ROCKFALL, Flag.ROCKFALL2)
                && Objects.stream().at(this).filter { it.isRockfall() }.isNotEmpty()
    }

    fun Tile.regionTile(): Tile {
        val (x1, y1) = Game.mapOffset()
        return derive(-x1, -y1)
    }

}

fun main() {
    TestScript().startScript()
}
