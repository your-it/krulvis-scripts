package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.RunePouch
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

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "", priv = true)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "rocks",
            description = "Click som rocks",
            optionType = OptionType.GAMEOBJECT_ACTIONS,
        ),
        ScriptConfiguration(
            name = "Extra info",
            description = "Here comes extra info \n new lines?",
            optionType = OptionType.INFO
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
    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks") }
    var path = emptyList<Edge<*>?>()
    var obj: GameObject? = null
    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        obj = Objects.stream().name("Blisterwood Tree").firstOrNull()
        log.info(obj?.tile?.toString() ?: "")
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
    //Tile(x=3635, y=3362, floor=0)
    //Tile(x=3633, y=3359, floor=0)

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

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
//            .addString("Top poly:") { "${Data.TOP_POLY.contains(Players.local().tile())}" }
            .addString("Runes:") { RunePouch.runes().joinToString { (id, a) -> "$id:$a" } }
//            .addString("Runes:") { RunePouchCtm.runes().joinToString { (id, a) -> "$id:$a" } }
            .build()
    }

    override fun paintCustom(g: Graphics) {
        script.obj?.tile?.drawOnScreen(g)
        g.setScale(1.0f)
    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestScript().startScript("127.0.0.1", "GIM", false)
}
