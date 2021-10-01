package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.*
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.service.WebWalkingService.drawEdgeList

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : ATScript() {
    override fun createPainter(): ATPaint<*> = TestWebPainter(this)

    var origin = Tile(3229, 3214, 0) //varrock mine

    var collisionMap: Array<IntArray> = emptyArray()

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3231, 3207, 0)
    var localPath: LocalPath = LocalPath(emptyList())
    var comp: Component? = null
    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks")!! }
    var path = emptyList<Edge<*>?>()
    var trapdoor: GameObject? = null
    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        Movement.walkTo(newDest)
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

    override fun paintCustom(g: Graphics) {
        val oldScale = g.getScale()
        script.origin.drawOnScreen(g)
        script.newDest.drawOnScreen(g)
        script.localPath.draw(g)
        script.path.drawEdgeList(g)
//        script.rocks.forEach {
//            it.tile.drawOnScreen(g, outlineColor = Color.GREEN)
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
