package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.blocked
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.loaded
import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.ClientContext
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.awt.Color
import java.awt.Graphics2D

@Script.Manifest(name = "TestWeb", description = "Some testing", version = "1.0")
class TestWeb : ATScript() {

    val tile = Tile(3275, 3428, 0)
    var doors = emptyList<GameObject>()
    var collisionMap: ICollisionMap? = null

    override val painter: ATPainter<*>
        get() = WebPainter(this)
    override val rootComponent: TreeComponent<*> = object : Leaf<TestWeb>(this, "TestLeaf") {
        override fun execute() {
            collisionMap = ctx.client().collisionMaps[ATContext.me.tile().floor()]
            println("Is blocked: ${tile.blocked(collisionMap)}, Is loaded: ${tile.loaded()}")
//            println("Is door: ${LocalPathFinder.getDoor(Tile(3107, 3162, 0), Flag.Rotation.WEST) != GameObject.NIL}")

//            doors = ctx.objects.toStream().name("Door").list()
//            LocalPathFinder.findPath(tile).traverse()
        }
    }

    override fun startGUI() {
        debugComponents = true
        started = true
    }
}

class WebPainter(script: TestWeb) : ATPainter<TestWeb>(script, 10) {

    override fun paint(g: Graphics2D) {
        var y = this.y
        debugTilesAroundMe(g)
        if (script.collisionMap != null) {
            drawSplitText(
                g,
                "OffsetX: ${script.collisionMap!!.offsetX}",
                "OffsetY: ${script.collisionMap!!.offsetY}",
                x,
                y
            )
        }

//        script.doors.forEach {
//            val t = it.tile()
//            drawTile(
//                g,
//                t,
//                text = "${it.name()}, Orient: ${it.orientation()}",
//                lineColor = null,
//                fillColor = null
//            )
//        }
        script.tile.drawOnScreen(
            g,
            null,
            fillColor = if (script.tile.blocked(script.collisionMap)) Color.RED else Color.GREEN
        )

    }

    fun debugTilesAroundMe(g: Graphics2D) {
        val t = script.ctx.players.local().tile()
        val p = t.floor()
        val flags = script.collisionMap ?: return

        val n = Tile(t.x(), t.y() + 1, p)
        val e = Tile(t.x() + 1, t.y(), p)
        val s = Tile(t.x(), t.y() - 1, p)
        val w = Tile(t.x() - 1, t.y(), p)

        val ne = Tile(t.x() + 1, t.y() + 1, p)
        val se = Tile(t.x() + 1, t.y() - 1, p)
        val sw = Tile(t.x() - 1, t.y() - 1, p)
        val nw = Tile(t.x() - 1, t.y() + 1, p)

        val tiles = listOf(n, e, s, w, ne, se, sw, nw)
        tiles.forEach { tile ->
            tile.drawCollisions(g, flags)
        }
    }

    override fun drawProgressImage(g: Graphics2D, height: Int) {
        TODO("Not yet implemented")
    }
}