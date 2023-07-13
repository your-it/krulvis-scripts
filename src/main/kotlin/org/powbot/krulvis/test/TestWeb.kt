package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : ATScript() {
    override fun createPainter(): ATPaint<*> = TestWebPainter(this)

    var rockTile = Tile(x=3020, y=9703, floor=0) //Amathyst mine spot
    var neighbor: Tile? = Tile(0,0,0) //Amathyst mine spot

    var collisionMap: TransientGetter2D<Int>? = null

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        collisionMap = Movement.collisionMap(me.tile().floor).flags()
        neighbor = rockTile.getWalkableNeighbor(allowSelf = false, checkForWalls = false)

        sleep(2000)
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        log.info("$e")
    }
}


class TestWebPainter(script: TestWeb) : ATPaint<TestWeb>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .build()
    }

    override fun paintCustom(g: Rendering) {
        val collisionMap = script.collisionMap
        if (collisionMap != null) {
            val neighbor = script.neighbor
            Players.local().tile().drawCollisions(collisionMap)
            val colorRock = if(script.rockTile.blocked(collisionMap)) Color.RED else Color.GREEN
            val colorNeighbor = if(script.neighbor?.blocked(collisionMap) == true) Color.RED else Color.GREEN
            script.rockTile.drawOnScreen(outlineColor = colorRock)
            neighbor?.drawOnScreen(outlineColor = colorNeighbor)
        }

    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestWeb().startScript("127.0.0.1", "banned", true)
}
