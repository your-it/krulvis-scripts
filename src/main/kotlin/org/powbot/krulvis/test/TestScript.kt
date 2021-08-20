package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.Condition.wait
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Constants.MOBILE_TAB_OPEN_BUTTON_TEXTURE_ID
import org.powbot.api.rt4.Constants.MOBILE_TAB_WINDOW_COMPONENT_ID
import org.powbot.api.rt4.Constants.MOBILE_TAB_WINDOW_WIDGET_ID
import org.powbot.api.rt4.Game.Tab
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPathFinder.isRockfall
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.selectors.GameObjectOption
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "testscript", version = "1.0d", description = "")
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

    val dest = Tile(3208, 3221, 2) //Lummy top bank
    val nearHopper = Tile(3748, 5673, 0) //Hopper in Motherlode
    val topMineFloor = Tile(3757, 5680, 0) //Hopper in Motherlode
    val oddRockfall = Tile(x = 3216, y = 3210, floor = 0)
    var flags = emptyArray<IntArray>()

    val rocks by lazy { getOption<List<GameObjectOption>>("rocks") ?: emptyList() }

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        if (rocks.isEmpty()) {
            log.info("Did not set rocks..")
        } else {
            log.info(rocks.joinToString())
        }
    }

    private var mobileViewport: Rectangle = Rectangle(-1, -1, -1, -1)

    fun pointInViewport(x: Int, y: Int, checkIfObstructed: Boolean = true): Boolean {
        val point = Point(x, y)
        if (checkIfObstructed) {

            //If there is one tab open get the component's bounds and check if point is in there
            if (Components.stream(601).texture(MOBILE_TAB_OPEN_BUTTON_TEXTURE_ID).firstOrNull() != null) {
                val tabComponent: Component = Widgets.component(
                    MOBILE_TAB_WINDOW_WIDGET_ID,
                    MOBILE_TAB_WINDOW_COMPONENT_ID
                )
                if (tabComponent.valid() && tabComponent.contains(point)) {
                    return false
                }
            }

            val optionBar = Chat.optionBarComponent();
            if (optionBar.valid() && optionBar.contains(point)) {
                return false
            }
        }

        if (mobileViewport.isEmpty()) {
            mobileViewport.setBounds(Game.viewport().boundingRect())
        }

        return mobileViewport.contains(point)
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

    fun Tile.globalTile(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }
}

class TestPainter(script: TestScript) : ATPainter<TestScript>(script, 10, 500) {
    override fun paint(g: Graphics, startY: Int) {
        var y = startY
        y = drawSplitText(g, "Tab: ", Game.tab().toString(), x, y)

    }

}

fun main() {
    TestScript().startScript(false)
}
