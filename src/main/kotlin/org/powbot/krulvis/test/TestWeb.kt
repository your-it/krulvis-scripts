package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : ATScript() {
    override fun createPainter(): ATPaint<*> = TestWebPainter(this)

    var origin = Tile(2912, 9968, 0) //varrock mine

    var collisionMap: TransientGetter2D<Int>? = null

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3090, 3245, 0)
    var localPath: LocalPath = LocalPath(emptyList())
    var comp: Component? = null
    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks")!! }
    var path = emptyList<Edge<*>?>()
    var trapdoor: GameObject? = null
    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//        localPath = LocalPathFinder.findPath(origin, newDest)
//        localPath = LocalPathFinder.findPath(Players.local().tile(), newDest)
        val nearest = Bank.getBank()
        log.info("Nearest bank: ${nearest}, tile=${nearest.tile()}")
        Bank.openNearest()
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
        val oldScale = g.getScale()
        if (script.collisionMap != null) {
            Players.local().tile().drawCollisions(script.collisionMap!!)
        }
        script.origin.drawOnScreen()
        script.newDest.drawOnScreen()
        script.localPath.draw()
//        script.rocks.forEach {
//            it.tile.drawOnScreen( outlineColor = Color.GREEN)
//        }
    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestWeb().startScript("127.0.0.1", "banned", true)
}
