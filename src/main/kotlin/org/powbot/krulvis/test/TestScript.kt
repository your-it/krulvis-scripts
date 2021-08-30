package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.Condition.wait
import org.powbot.api.action.ActionWaiter
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.VarpbitChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics
import java.util.concurrent.TimeUnit

@ScriptManifest(name = "testscript", version = "1.0.1", description = "")
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "rocks",
            description = "Click som rocks",
            optionType = OptionType.GAMEOBJECTS,
            defaultValue = "[{\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5678,\"z\":0},\"x\":3756,\"y\":5678}}]"
        ),
        ScriptConfiguration(
            name = "rocks1",
            description = "Want to have rocks?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "rocks2",
            description = "Want to have 0?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = "rocks3",
            description = "Select",
            optionType = OptionType.STRING,
            defaultValue = "2",
            allowedValues = ["1", "2", "3"]
        ),
    ]
)
class TestScript : ATScript() {
    override val painter: ATPainter<*> = TestPainter(this)

    //    val origin = Tile(3290, 3358, 0) //varrock mine
//    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3283, 3428, 0)
    var path: LocalPath = LocalPath(emptyList())

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//        val b = Bank.getNearestBank()
//        log.info("Bank=$b")
//        log.info("Open=${b.open()}")
        if (!Bank.opened()) {
            val b = Objects.stream().name("Bank booth").nearest().first()
            log.info("Interact: ${interact(b, "Bank")}")
            wait { Bank.opened() }
        }
    }

    fun interact(go: GameObject, action: String): Boolean {
        return interact(go, Menu.filter(action, go.name), true)
    }

    /**
     * {@inheritDoc}
     */
    fun interact(go: GameObject, f: Filter<MenuCommand>, hasMenu: Boolean): Boolean {
        if (!go.valid()) {
            println("${javaClass.simpleName} is not valid ")
            return false
        }

        val actionWaiter = if (f !is Menu.TextFilter) null else ActionWaiter(f.action!!, f.option)

        return try {
            if (click(go, f, hasMenu)) {
                actionWaiter == null || actionWaiter.waitForAction(2, TimeUnit.SECONDS)
            } else {
                log.info("Click failed...")
                false
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            false
        } finally {
            actionWaiter?.unregister()
        }
    }

    /**
     * {@inheritDoc}
     */
    fun click(go: GameObject, f: Filter<MenuCommand>, hasMenu: Boolean): Boolean {
        val point = go.calculateScreenPosition().call()
        val action = if (f !is Menu.TextFilter) "null" else f.action
        val skipMenu = !hasMenu || (!Game.singleTapEnabled() && go.actions().indexOf(action) == 0)
        if (point.valid() && Input.tap(point)) {
            return skipMenu || (wait({ Menu.opened() }, 10, 60) && Menu.click(f))
        }
        return false
    }


    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        if (e.opcode() == GameActionOpcode.InteractObject) {
            val tile = Tile(e.var0, e.widgetId).globalTile()
            log.info("Interacted with obj at tile=${tile} $e")
        } else {
            log.info("GameActionEvent $e")
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onVarpbitChangedEvent(evt: VarpbitChangedEvent) {
//        log.info("Varpbit change: index=${evt.index}, newValue=${evt.newValue}, prevValue=${evt.previousValue}")
    }

    fun Tile.globalTile(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }
}

class TestPainter(script: TestScript) : ATPainter<TestScript>(script, 10, 500) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        y = drawSplitText(g, "Single-tab", Game.singleTapEnabled().toString(), x, y)
        script.newDest.drawOnScreen(g)
        script.path.draw(g)

        return y
    }

}

fun main() {
    TestScript().startScript(true)
}
