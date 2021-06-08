package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.tithe.Data.WATER_CAN_FULL
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powerbot.bot.rt4.client.internal.IActor
import org.powerbot.script.*
import org.powerbot.script.rt4.*
import org.powerbot.script.rt4.Interactive
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Point
import java.awt.Rectangle
import java.util.concurrent.Callable

@Script.Manifest(name = "TestScript", description = "Some testing", version = "1.0")
class TestScript : ATScript() {


    val tile = Tile(2986, 3240, 0)

    override val painter: ATPainter<*>
        get() = Painter(this)

    var patches = listOf<Patch>()
    override val rootComponent: TreeComponent<*> = object : Leaf<TestScript>(this, "TestLeaf") {
        override fun execute() {
//            patches = ctx.objects.toStream(25).filter { it.isPatch() }.list().mapIndexed { i, go -> Patch(go, i) }
            if (ctx.inventory.selectedItem() == Item.NIL) {
                ATContext.ctx.inventory.toStream().id(WATER_CAN_FULL).findFirst().ifPresent {
                    it.interact("Use")
                }
            } else if (ctx.inventory.selectedItem().id() == WATER_CAN_FULL) {
                Utils.waitFor(Utils.short()) { ATContext.ctx.inventory.selectedItem().id() == WATER_CAN_FULL }
                val basket = ctx.objects.toStream(25).name("Crates").nearest().findFirst()
                if (basket.isPresent) {
                    val b = basket.get()
                    val interaction = b.testInteract("Use")
                    println("Interaction: Use was successful: $interaction")
                }
            }
        }
    }

    fun Interactive.testInteract(action: String): Boolean {
        if (!valid() || !inViewport()) {
            return false
        }
        val position: Callable<Point> = calculateScreenPosition()
        return try {
            //			if (ctx.client().isMobile() && (Interactive.this instanceof Actor || Interactive.this instanceof GameObject)) {
            //				// We need to do this in order to load the menu
            //				final Point target = position.call();
            //				ctx.input.move(target);
            //				ctx.input.drag(new Point(target.x + 3, target.y + 3), false);
            //				Condition.sleep(Random.nextInt(10, 20));
            //			}
            if (ctx.input.move(position.call())) {
                val clicked = ctx.menu.testClick(Menu.filter(action))
                println("Clicked: $clicked")
                clicked
            } else false
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        } finally {
            if (ctx.menu.opened()) {
                ctx.menu.close()
            }
            false
        }
    }

    private fun Menu.testClick(filter: Filter<in MenuCommand?>, click: Boolean = true): Boolean {
        val client = ctx.client() ?: return false
        if (ctx.client().isMobile && !ctx.client().isMenuOpen) {
            // TODO: set tap to click to on
            if (!ctx.input.click(true)) return false
        }
        if (!Condition.wait({ indexOf(filter) != -1 }, 10, 60)) {
            return false
        }
        val slot: Int = indexOf(filter)
        println("First slot check: $slot")
        if (click && !client.isMenuOpen && slot == 0) {
            return ctx.input.click(true)
        }
        if (!client.isMenuOpen) {
            println("Opening menu")
            if (!ctx.input.click(false)) {
                println("Failed to open menu")
                return false
            }
        }
        if (!Condition.wait({ client.isMenuOpen }, 10, 60)) {
            println("Menu still not open...")
            return false
        }
        val headerOffset = if (ctx.client().isMobile) 29 else 19
        val itemOffset = if (ctx.client().isMobile) 24 else 15
        val rectangle =
            Rectangle(client.menuX, client.menuY + headerOffset + slot * itemOffset, client.menuWidth, itemOffset)
        Condition.sleep(Random.hicks(slot))
        return if (!ctx.input.move(
                Random.nextInt(rectangle.x, rectangle.x + rectangle.width),
                Random.nextInt(rectangle.y, rectangle.y + rectangle.height)
            ) || !client.isMenuOpen
        ) {
            println("Clicking went wrong")
            false
        } else client.isMenuOpen && Condition.wait({
            rectangle.contains(
                ctx.input.location
            )
        }, 10, 60) && (!click || ctx.input.click(true))
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

}

class Painter(script: TestScript) : ATPainter<TestScript>(script, 10) {
    override fun paint(g: Graphics2D) {
        val patches = script.patches
        patches.forEach {
            if (!it.isNill) {
                val color =
                    if (it.isNill) Color.BLACK else if (it.isEmpty()) Color.CYAN else if (it.isDone()) Color.GREEN else if (!it.needsWatering()) Color.YELLOW else Color.BLUE
                drawTile(g, it.go.tile(), text = it.toString(), lineColor = color, mapColor = color)
            }
        }
        y += yy
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