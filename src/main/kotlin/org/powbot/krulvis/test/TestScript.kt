package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.*
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.service.WebWalkingService.drawEdgeList

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "")
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "rocks",
            description = "Click som rocks",
            optionType = OptionType.GAMEOBJECTS,
            defaultValue = "[{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":424,\"mouseY\":242,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":50,\"widgetId\":56,\"tile\":{\"floor\":1,\"x\":1570,\"y\":3480,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":449,\"mouseY\":238,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":51,\"widgetId\":56,\"tile\":{\"floor\":1,\"x\":1571,\"y\":3480,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":377,\"mouseY\":311,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":59,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3483,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":403,\"mouseY\":318,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":58,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3482,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":453,\"mouseY\":411,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":58,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3482,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":514,\"mouseY\":326,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":58,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3482,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":487,\"mouseY\":244,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":59,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3483,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":658,\"mouseY\":285,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":68,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3492,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":666,\"mouseY\":281,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":68,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3492,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":412,\"mouseY\":441,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":69,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3493,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":501,\"mouseY\":401,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":51,\"widgetId\":70,\"tile\":{\"floor\":1,\"x\":1571,\"y\":3494,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":565,\"mouseY\":335,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":50,\"widgetId\":70,\"tile\":{\"floor\":1,\"x\":1570,\"y\":3494,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":563,\"mouseY\":336,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":69,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3493,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":514,\"mouseY\":324,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":68,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3492,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"}]"
        ),
        ScriptConfiguration(
            name = "tile",
            description = "Get Tile?",
            optionType = OptionType.TILE,
            defaultValue = "{\"floor\":0,\"x\":1640,\"y\":3944,\"rendered\":true}"
        ),
        ScriptConfiguration(
            name = "rocks1",
            description = "WIDGETS?",
            optionType = OptionType.WIDGETS,
        ),
        ScriptConfiguration(
            name = "rocks1",
            description = "ALL ACTIONS?",
            optionType = OptionType.GAME_ACTIONS,
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
    override fun createPainter(): ATPaint<*> = TestPainter(this)

    var origin = Tile(3229, 3214, 0) //varrock mine

    var collisionMap: Array<IntArray> = emptyArray()

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3231, 3207, 0)
    var localPath: LocalPath = LocalPath(emptyList())
    var comp: Component? = null

    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks")!! }

    var redWoods: List<GameObject> = emptyList()
    var path = emptyList<Edge<*>?>()

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//        redWoods = Objects.stream().name("Redwood").list()
//        log.info("Orientation of nearest: ${redWoods.sortedBy { it.distance() }.first().orientation()}")
//        val player = WebWalking.playerState.player(false)
//        if (player != null) {
//            log.info("Getting path to nearest bank")
////            path = WebWalkingService.getPath(player, WebTile(2901, 9809, 0))
//            path = WebWalkingService.getPathToNearestBank(player, enableTeleports = false)
//            log.info("path size=${path.size}")
//            path.forEach {
//                log.info("$it")
//            }
//        } else {
//            log.info("Player is null")
//        }
        val lootNames = listOf("Blue dragonhide", "Dragon bones")
        GroundItems.stream()
            .filtered { lootNames.contains(it.name()) || GrandExchange.getItemPrice(it.id()) * it.stackSize() >= 1000 }
            .nearest()
            .list().forEach {
                log.info("Found ${it.name()} at=${it.tile}")
            }
        sleep(2000)
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        log.info("$e")
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {

    }

}

class TestPainter(script: TestScript) : ATPaint<TestScript>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("Comp:") { "widgetId=${script.comp?.widgetId()}, ${script.comp}" }
            .addString("CurrTile:") {
                "blocked=${
                    Players.local().tile().blocked(Movement.collisionMap(0).flags())
                }, fire=${Objects.stream().at(Players.local().tile()).firstOrNull { it.name == "Fire" }?.name}"
            }
            .trackInventoryItem(12012)
            .build()
    }

    override fun paintCustom(g: Graphics) {
        val oldScale = g.getScale()
        script.origin.drawOnScreen(g)
        script.newDest.drawOnScreen(g)
        script.localPath.draw(g)
        script.path.drawEdgeList(g)
        script.redWoods.forEach {
            it.tile.drawOnScreen(g, outlineColor = Color.CYAN)
        }
        script.rocks.forEach {
            it.tile.drawOnScreen(g, outlineColor = Color.GREEN)
        }
        g.setColor(Color.CYAN)
    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestScript().startScript(true)
}
