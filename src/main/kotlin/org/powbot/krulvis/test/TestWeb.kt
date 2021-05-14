package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.extensions.walking.local.LocalPath
import org.powbot.krulvis.api.extensions.walking.local.LocalPath.Companion.getNext
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartEdge
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.walking.PBWebWalkingService
import org.powbot.krulvis.walking.Walking
import org.powerbot.bot.rt4.client.internal.ICollisionMap
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.awt.Color
import java.awt.Graphics2D

@Script.Manifest(name = "TestWeb", description = "Some testing", version = "1.0")
class TestWeb : ATScript() {

    val from = Tile(3286, 3368, 0)
    val tile = Tile(3291, 3392, 0)
    var path = LocalPath(emptyList())
    var doors = emptyList<GameObject>()
    var collisionMap: ICollisionMap? = null

    val outside = Tile(3281, 3191, 0)
    val b = Tile(3244, 3209, 0)
    val a = Tile(3245, 3193, 0)

    var walkingToTanner = true

    override val painter: ATPainter<*>
        get() = WebPainter(this)
    override val rootComponent: TreeComponent<*> = object : Leaf<TestWeb>(this, "TestLeaf") {
        override fun execute() {
            collisionMap = ctx.client().collisionMaps[ATContext.me.tile().floor()]
            doors = ctx.objects.toStream().name("Door").list()
//            println("Is blocked: ${tile.blocked(collisionMap)}, Is loaded: ${tile.loaded()}")
//            walk(tile)
//            PBWebWalkingService.walkTo(tile, false)
            walkToTanner()
//            testPathFinder()
        }
    }

    fun testPathFinder() {
        Walking.logger.info("Distance: ${a.distance()}")
        path = LocalPathFinder.findPath(a)
        val next = path.actions.getNext() ?: return
        Walking.logger.info("LocalTraverse next: $next")
        if (next is StartEdge && next.destination.distance() <= 1) {
            Walking.logger.info("Standing next to StartTile: $next")
            return
        }
        if (next.execute()) {
            sleep(600)
        }
    }


    fun walkToTanner() {
        if (walkingToTanner) {
            if (a.distance() > 0) {
                PBWebWalkingService.walkTo(a, false)
//                ctx.movement.walkTo(tanner, false)
            } else {
                walkingToTanner = false
            }
        } else {
            val other = b
            if (other.distance() > 0) {
                PBWebWalkingService.walkTo(b, false)
//                ctx.movement.walkTo(other, false)
            } else {
                walkingToTanner = true
            }
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

        script.doors.forEach {
            val t = it.tile()
            drawTile(
                g,
                t,
                text = "${it.name()}, Orient: ${it.orientation()}",
                lineColor = null,
                fillColor = null
            )
        }
        if (script.collisionMap != null) {
            script.tile.drawOnScreen(
                g,
                null,
                fillColor = if (script.tile.blocked(script.collisionMap)) Color.RED else Color.GREEN
            )
            val path = script.path
            if (path.isNotEmpty()) {
                path.draw(g)
            }
        }

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