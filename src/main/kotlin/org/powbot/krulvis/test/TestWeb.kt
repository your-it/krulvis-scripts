package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.toRegionTile
import org.powbot.krulvis.api.extensions.walking.local.LocalPath
import org.powbot.krulvis.api.extensions.walking.local.LocalPath.Companion.getNext
import org.powbot.krulvis.api.extensions.walking.local.LocalPathFinder
import org.powbot.krulvis.api.extensions.walking.local.nodes.LocalEdge
import org.powbot.krulvis.api.extensions.walking.local.nodes.StartEdge
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.walking.PBWebWalkingService
import org.powbot.krulvis.walking.Walking
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.awt.Color
import java.awt.Graphics2D

@Script.Manifest(name = "TestWeb", description = "Some testing", version = "1.0")
class TestWeb : ATScript() {

    val motherload1 = Tile(3734, 5679, 0)
    val motherload2 = Tile(3729, 5678, 0)
    val doors1 = Tile(2615, 3304, 0)
    val doors2 = Tile(2610, 3305, 0)
    val debugFence = Tile(3143, 3291, 0)
    var path = LocalPath(emptyList())
    var doors = emptyList<GameObject>()
    var collisionMap: Array<IntArray> = emptyArray()
    var localEdge: LocalEdge = StartEdge(Tile(3731, 5682), motherload2)
    var neighbors = emptyList<LocalEdge>()

    val draynor = Tile(3281, 3191, 0)
    val b = Tile(3244, 3209, 0)
    val a = Tile(3245, 3193, 0)

    var walkingToTanner = true

    override val painter: ATPainter<*>
        get() = WebPainter(this)
    override val rootComponent: TreeComponent<*> = object : Leaf<TestWeb>(this, "TestLeaf") {
        override fun execute() {
            collisionMap = ctx.client().collisionMaps[me.tile().floor()].flags
            path = LocalPathFinder.findPath(doors1, doors2)
            LocalPathFinder.cachedFlags = collisionMap
//            neighbors = StartEdge(Tile(3731, 5682, 0), tile).getNeighbors()
            if (path.isNotEmpty() && motherload2.distance() > 1) {
//                path.traverse()
            }
        }
    }

    fun testPathFinder() {
        Walking.logger.info("Distance: ${motherload2.distance()}")
        val start = System.currentTimeMillis()
        path = LocalPathFinder.findPath(motherload2)
        Walking.logger.info("LocalPathFinder took: ${Timer.formatTime(System.currentTimeMillis() - start)}")
        val next = path.actions.getNext() ?: return
        Walking.logger.info("LocalTraverse next: $next")
        if (next is StartEdge && next.destination.distance() <= 1) {
            Walking.logger.info("Standing next to StartTile: $next")
            return
        }
    }


    fun walkBackAndForth() {
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
//        debugTilesAroundMe(g)
        if (script.collisionMap.isNotEmpty()) {
            drawSplitText(g, "Tile loaded: ", script.motherload2.loaded().toString(), x, y)
            y += yy
            drawSplitText(g, "RegionTile: ", script.motherload2.toRegionTile().toString(), x, y)
            script.motherload2.drawOnMap(g, Color.CYAN)
            script.motherload2.drawOnScreen(
                g,
                null,
                fillColor = if (script.motherload2.blocked(script.collisionMap)) Color.RED else Color.GREEN
            )
            val path = script.path
            if (path.isNotEmpty()) {
                path.draw(g)
            }
            val neighbors = script.neighbors
            if (neighbors.isNotEmpty()) {
                neighbors.forEach { it.destination.drawOnScreen(g, null, Color.ORANGE) }
            }
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
        debugTilesAroundMe(g)

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