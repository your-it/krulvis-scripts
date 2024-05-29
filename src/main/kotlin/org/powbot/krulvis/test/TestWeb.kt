package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.Locatable
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Rendering
import org.powbot.util.TransientGetter2D

@ScriptManifest(name = "test Web", version = "1.0.1", description = "", priv = true)
class TestWeb : ATScript() {
    override fun createPainter(): ATPaint<*> = TestWebPainter(this)

    var patchTile = Tile(1267, 3727, floor = 0)
    var neighbor: Tile? = Tile(0, 0, 0) //Amathyst mine spot

    var patch: GameObject? = null
    var collisionMap: TransientGetter2D<Int>? = null

    fun getPatchObj() = Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Hopper").action("Deposit").nearest().firstOrNull()

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        collisionMap = Movement.collisionMap(me.tile().floor).flags()
        patch = getPatchObj()

        val walkableNeighbors = patch?.getWalkableNeighbors(allowSelf = false, checkForWalls = false) ?: emptyList()

        logger.info("Patch=$patch, neighbors=${walkableNeighbors.joinToString()}")
        neighbor = walkableNeighbors.firstOrNull()

        sleep(2000)
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        logger.info("$e")
    }

    @JvmOverloads
    fun Locatable.getWalkableNeighbors(
            allowSelf: Boolean = true,
            diagonalTiles: Boolean = false,
            checkForWalls: Boolean = true,
    ): MutableList<Tile> {
        val t = tile()
        val x = t.x()
        val y = t.y()
        val f = t.floor()
        val cm = Movement.collisionMap(t.floor).flags()
        //the tile itself is not blocked, just return that...
        if (allowSelf && !t.blocked(cm)) {
            return mutableListOf(t)
        }

        val n = Tile(x, y + 1, f)
        val e = Tile(x + 1, y, f)
        val s = Tile(x, y - 1, f)
        val w = Tile(x - 1, y, f)
        val straight = listOf(n, e, s, w)
        val straightFlags = listOf(Flag.W_S, Flag.W_W, Flag.W_N, Flag.W_E)
        val ne = Tile(x + 1, y + 1, f)
        val se = Tile(x + 1, y - 1, f)
        val sw = Tile(x - 1, y - 1, f)
        val nw = Tile(x - 1, y + 1, f)
        val diagonal = listOf(ne, se, sw, nw)

        val walkableNeighbors = mutableListOf<Tile>()
        straight.forEachIndexed { index, tile ->
            logger.info("Blocked[$index] tile=$tile blocked=${tile.blocked(cm)}, collisionFlag=${tile.collisionFlag(cm)}")
        }
        walkableNeighbors.addAll(straight.filterIndexed { i, it ->
            if (checkForWalls) {
                !it.blocked(
                        cm,
                        straightFlags[i]
                )
            } else !it.blocked(cm)
        })

        if (diagonalTiles) {
            walkableNeighbors.addAll(diagonal.filter { !it.blocked(cm) })
        }
        return walkableNeighbors
    }
}


class TestWebPainter(script: TestWeb) : ATPaint<TestWeb>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
                .addString("Reachable") { script.neighbor?.reachable().toString() }
                .build()
    }

    override fun paintCustom(g: Rendering) {
        val collisionMap = script.collisionMap
        if (collisionMap != null) {
            val neighbor = script.neighbor
            Players.local().tile().drawCollisions(collisionMap)
            val colorTile = if (script.patchTile.blocked(collisionMap)) Color.RED else Color.GREEN
            val colorPatch = if (script.patch?.tile?.blocked(collisionMap) == true) Color.ORANGE else Color.GREEN
            val colorNeighbor = if (script.neighbor?.blocked(collisionMap) == true) Color.RED else Color.GREEN
//            script.patchTile.drawOnScreen(outlineColor = colorTile)
            script.patch?.tile?.drawOnScreen(outlineColor = colorPatch)
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
