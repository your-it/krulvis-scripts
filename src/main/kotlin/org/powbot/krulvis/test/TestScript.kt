package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Constants.BANK_CULINAROMANCER_LOCATION
import org.powbot.api.rt4.Constants.BANK_UNREACHABLES
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.*
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "", priv = true)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "rocks",
            description = "Click som rocks",
            optionType = OptionType.GAMEOBJECT_ACTIONS,
            defaultValue = "[{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":424,\"mouseY\":242,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":50,\"widgetId\":56,\"tile\":{\"floor\":1,\"x\":1570,\"y\":3480,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":449,\"mouseY\":238,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":51,\"widgetId\":56,\"tile\":{\"floor\":1,\"x\":1571,\"y\":3480,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":377,\"mouseY\":311,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":59,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3483,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":403,\"mouseY\":318,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":58,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3482,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":453,\"mouseY\":411,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":58,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3482,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":514,\"mouseY\":326,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":58,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3482,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":487,\"mouseY\":244,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":59,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3483,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":658,\"mouseY\":285,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":68,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3492,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":666,\"mouseY\":281,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":68,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3492,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":412,\"mouseY\":441,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":52,\"widgetId\":69,\"tile\":{\"floor\":1,\"x\":1572,\"y\":3493,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":501,\"mouseY\":401,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":51,\"widgetId\":70,\"tile\":{\"floor\":1,\"x\":1571,\"y\":3494,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":565,\"mouseY\":335,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":50,\"widgetId\":70,\"tile\":{\"floor\":1,\"x\":1570,\"y\":3494,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29668,\"interaction\":\"Cut\",\"mouseX\":563,\"mouseY\":336,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":69,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3493,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"},{\"id\":29670,\"interaction\":\"Cut\",\"mouseX\":514,\"mouseY\":324,\"rawEntityName\":\"<col=ffff>Redwood\",\"rawOpcode\":3,\"var0\":48,\"widgetId\":68,\"tile\":{\"floor\":1,\"x\":1568,\"y\":3492,\"rendered\":true},\"name\":\"Redwood\",\"strippedName\":\"Redwood\"}]"
        ),
        ScriptConfiguration(
            name = "Double",
            description = "Get Double?",
            optionType = OptionType.DOUBLE,
//            defaultValue = "{\"floor\":0,\"x\":1640,\"y\":3944,\"rendered\":true}"
        ),
        ScriptConfiguration(
            name = "tile",
            description = "Get Tile?",
            optionType = OptionType.TILE,
            defaultValue = "{\"floor\":0,\"x\":1640,\"y\":3944,\"rendered\":true}"
        ),
        ScriptConfiguration(
            name = "rocks1",
            description = "NPCS?",
            optionType = OptionType.NPC_ACTIONS,
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

    var origin = Tile(3212, 3216, 0) //varrock mine

    var collisionMap: Array<IntArray> = emptyArray()

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3231, 3207, 0)
    var localPath: LocalPath = LocalPath(emptyList())
    var comp: Component? = null
    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks")!! }
    var path = emptyList<Edge<*>?>()
    var obj: GameObject? = null
    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//        if (check()) {
//            log.info("CHECK")
//            execute()
//        }
        sleep(2000)
    }

    fun check(): Boolean {
        return !Game.singleTapEnabled()
    }

    fun execute(): Boolean {
        val button = Components.stream(601).action("Set Function").first()
        if (button.actions().contains("Toggle single-tap mode")) {
            log.info("Found single-tap in function button")
            return button.interact("Toggle single-tap mode") && Condition.wait { !Game.singleTapEnabled() }
        } else if (!Chat.chatting()) {
            log.info("Should set function first")
            if (!Menu.opened()) {
                val point = button.boundingRect().nextPoint()
                Input.press(point)
                sleep(Random.nextInt(1500, 2000))
                Input.release(point)
            } else Menu.click { it.action == "Set Function" }
        } else if (Chat.completeChat("Single-tap")) {
            Condition.wait { Components.stream(601).action("Toggle single-tap mode").isNotEmpty() }
        }
        return false
    }

    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        log.info("$e")
    }

    @com.google.common.eventbus.Subscribe
    fun onMsg(e: MessageEvent) {
        log.info("MSG: \n Type=${e.type}, msg=${e.message}")
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        if (!painter.paintBuilder.trackingInventoryItem(evt.itemId)) {
//            painter.paintBuilder.trackInventoryItem(evt)
        }
    }

}

class TestPainter(script: TestScript) : ATPaint<TestScript>(script) {

    fun lampComp() = Components.stream(240).firstOrNull { it.actions().contains("Woodcutting") }

    fun confirmButton(): Component? {
        return Components.stream(240).action("Confirm").firstOrNull()
    }

    fun skillButton(): Component? {
        return Components.stream(240).action("Woodcutting").firstOrNull()
    }

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
//            .addString("Top poly:") { "${Data.TOP_POLY.contains(Players.local().tile())}" }
            .addString("skillButton:") { "${skillButton()?.actions()}" }
            .addString("confirmButton:") { "${confirmButton()?.tooltip()}" }
            .addString("Can click confirm:") {
                "${
                    confirmButton()?.components()?.any { it.text().contains("Woodcutting") }
                }"
            }
//            .addString("Can cast water strike:") { "${Magic.Spell.WATER_STRIKE.canCast()}" }
            .withTotalLoot(true)
            .build()
    }

    override fun paintCustom(g: Graphics) {
//        val oldScale = g.getScale()
//        script.origin.drawOnScreen(g)
//        script.newDest.drawOnScreen(g)
//        script.localPath.draw(g)
//        script.path.drawEdgeList(g)
////        script.rocks.forEach {
////            it.tile.drawOnScreen(g, outlineColor = Color.GREEN)
////        }
        g.setScale(1.0f)
    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestScript().startScript("127.0.0.1", "GIM", true)
}
