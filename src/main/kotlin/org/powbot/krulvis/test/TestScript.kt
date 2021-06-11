package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.toRegionTile
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.LastMade.stoppedMaking
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Patch
import org.powerbot.bot.rt4.client.internal.IActor
import org.powerbot.script.*
import org.powerbot.script.rt4.*
import java.awt.Graphics2D
import java.awt.Rectangle

@Script.Manifest(name = "TestScript", description = "Some testing", version = "1.0")
class TestScript : ATScript(), GameActionListener {


    val tile = Tile(2986, 3240, 0)

    override val painter: ATPainter<*>
        get() = Painter(this)

    var patches = listOf<Patch>()
    override val rootComponent: TreeComponent<*> = object : Leaf<TestScript>(this, "TestLeaf") {
        override fun execute() {
        }
    }

    val inputTexts = listOf(
        "enter message to", "enter amount", "set a price for each item",
        "enter name", "how many do you wish to buy", "please pick a unique display name",
        "how many charges would you like to add", "enter the player name",
        "what would you like to buy"
    )

    fun waitingForInput(): Boolean {
        val widget = ctx.widgets.widget(162).takeIf { w ->
            w.any { wc ->
                val text = wc.text().toLowerCase()
                wc.visible() && inputTexts.any { it in text }
            }
        }
        return widget?.any { it.visible() && it.text().endsWith("*") } ?: false
    }

    override fun startGUI() {
        debugComponents = true
        started = true
    }

    override fun stop() {
        painter.saveProgressImage()
    }

    override fun onAction(evt: GameActionEvent) {
        val tithe = ctx.objects.toStream().id(evt.id).nearest().findFirst()

        println("Var0: ${evt.var0}, Interaction: ${evt.interaction}, ID: ${evt.id}, Name: ${evt.rawEntityName}, OpCode: ${evt.rawOpcode}")
        if (tithe.isPresent) {
            val localTile = tithe.get().tile().toRegionTile()
            println("Nearest ${evt.rawEntityName} farm: Local X: ${localTile.x()}, Y: ${localTile.y()}")
        }
    }

}

class Painter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics2D) {
        drawSplitText(g, "Stopped making: ", stoppedMaking(227).toString(), x, y)
    }


    override fun drawProgressImage(g: Graphics2D, startY: Int) {
        var y = startY
        g.drawString("Test string", x, y)
        y += yy
    }
}