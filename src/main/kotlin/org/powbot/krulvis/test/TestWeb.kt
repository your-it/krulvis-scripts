package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
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
import java.awt.Graphics2D

@Script.Manifest(name = "TestWeb", description = "Some testing", version = "1.0")
class TestWeb : ATScript() {

    val tile = Tile(3242, 3440, 0)
    var doors = emptyList<GameObject>()
    var collisionMap: ICollisionMap? = null

    override val painter: ATPainter<*>
        get() = WebPainter(this)
    override val rootComponent: TreeComponent<*> = object : Leaf<TestWeb>(this, "TestLeaf") {
        override fun execute() {
//            collisionMap = ctx.client().collisionMaps[ATContext.me.tile().floor()]
//            doors = ctx.objects.toStream().name("Door").list()
//            println("Doors found: ${doors.size}")
            val nearestBank = ctx.bank.getNearestBank()
            ATContext.debug("Opening bank: ${nearestBank.name}")
            nearestBank.open()
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
        drawTile(g, script.tile)
        script.doors.forEach {
            val t = it.tile()
            drawTile(g, t, text = "${it.name()}, Orient: ${it.orientation()}")

        }
    }

    override fun drawProgressImage(g: Graphics2D, height: Int) {
        TODO("Not yet implemented")
    }
}