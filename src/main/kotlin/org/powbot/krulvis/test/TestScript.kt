package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powerbot.bot.rt4.client.internal.IActor
import org.powerbot.script.Script
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Actor
import java.awt.Graphics2D

@Script.Manifest(name = "TestScript", description = "Some testing", version = "1.0")
class TestScript : ATScript() {


    override val painter: ATPainter<*>
        get() = Painter(this)
    override val rootComponent: TreeComponent<*> = object : Leaf<TestScript>(this, "TestLeaf") {
        override fun execute() {
//            if (ctx.bank.opened()) {
//                return
//            }
//            val bank = ctx.bank.getNearestBank()
//            ATContext.debug("Nearest bank: $bank")
//            bank.open()
            val colMap = ctx.client().collisionMaps[ATContext.me.tile().floor()]
            val offset = ctx.game.mapOffset()
            println("CollisionMap: ${colMap.flags.size}, offSet: $offset")
        }
    }

    override fun startGUI() {
        debugComponents = true
        started = true
    }

    override fun stop() {
        painter.saveProgressImage()
    }

}

class Painter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics2D) {
        val ct = script.ctx.players.local().tile()
        if (ct != null) {
            ct.drawOnMap(g)
            ct.drawCircleOnMap(g, 5)
        }
    }

    fun realOrientation(): Int {
        val player = script.ctx.players.local()
        val method = Actor::class.java.getDeclaredMethod("getActor")
        method.isAccessible = true
        val actor = method.invoke(player) as IActor
        return actor.orientation
    }

    fun correctOrientation(): Int {
        val ori = realOrientation()
        return when (ori % 256) {
            255 -> ori + 1
            1 -> ori - 1
            else -> ori
        }
    }

    fun facingTile(): Tile {
        val orientation = realOrientation()
        val t: Tile = script.ctx.players.local().tile()
        when (orientation) {
            4 -> return Tile(t.x(), t.y() + 1, t.floor())
            6 -> return Tile(t.x() + 1, t.y(), t.floor())
            0 -> return Tile(t.x(), t.y() - 1, t.floor())
            2 -> return Tile(t.x() - 1, t.y(), t.floor())
        }
        return Tile.NIL
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
        g.drawString("Test string", x, y)
        y += yy
    }
}