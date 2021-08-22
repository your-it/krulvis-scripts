package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.Condition.wait
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Constants.MOBILE_TAB_OPEN_BUTTON_TEXTURE_ID
import org.powbot.api.rt4.Constants.MOBILE_TAB_WINDOW_COMPONENT_ID
import org.powbot.api.rt4.Constants.MOBILE_TAB_WINDOW_WIDGET_ID
import org.powbot.api.rt4.Game.Tab
import org.powbot.api.rt4.walking.Walking
import org.powbot.api.rt4.walking.local.Flag
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.LocalPathFinder.isRockfall
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.selectors.GameObjectOption
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.extensions.items.Item.Companion.HAMMER
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.miner.Data
import org.powbot.krulvis.smither.Smithable
import org.powbot.mobile.drawing.Graphics

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

    val origin = Tile(3287, 3204, 0)
    val dest = Tile(3293, 3228, 0) //Lummy top bank
    val nearHopper = Tile(3748, 5673, 0) //Hopper in Motherlode
    val topMineFloor = Tile(3757, 5680, 0) //Hopper in Motherlode
    val oddRockfall = Tile(x = 3216, y = 3210, floor = 0)
    var flags = emptyArray<IntArray>()

    val rocks by lazy { getOption<List<GameObjectOption>>("rocks") ?: emptyList() }
    var path: LocalPath = LocalPath(emptyList())

    val parent = 270

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        log.info("Depositing=${Bank.depositAllExcept(HAMMER, Bar.MITHRIL.id)}")
    }

    val northOfLadder = Tile(3755, 5675, 0)
    val ladderTile = Tile(3755, 5674, 0)
    fun escapeTopFloor(destination: Tile): Boolean {
        val pos = Players.local().tile()
        if (Data.TOP_CENTER_ML.distance() <= 8 && LocalPathFinder.findPath(pos, destination, true).isEmpty()) {
            if (northOfLadder.distance() <= 6 && LocalPathFinder.findPath(pos, northOfLadder, true).isNotEmpty()) {
                if (ladderTile.matrix().interact("Climb")) {
                    return Utils.waitFor {
                        LocalPathFinder.findPath(Players.local().tile(), destination, true).isNotEmpty()
                    }
                }
            } else {
                Movement.walkTo(northOfLadder)
            }
            return false
        }
        return true
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
    fun onInventoryChangeEvent(e: InventoryChangeEvent) {
        log.info("Inv change event for id=${e.itemId}, change=${e.quantityChange}")
    }

    fun Tile.globalTile(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }
}

class TestPainter(script: TestScript) : ATPainter<TestScript>(script, 10, 500) {
    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        script.dest.drawOnScreen(g)
        script.path.draw(g)

        return y
    }

}

fun main() {
    TestScript().startScript(true)
}
