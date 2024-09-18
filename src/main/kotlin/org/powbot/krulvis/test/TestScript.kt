package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Rendering

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

    var cupboards: List<GameObject> = emptyList()

    var guardian: Npc = Npc.Nil

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
//        val comps = Components.stream(406).text("Zamorak").toList()
//        comps.forEach {
//            logger.info("comp=$it")
//            logger.info("kc=${Widgets.widget(it.widgetId()).component(it.index() + 5).text()}")
//        }
//        logger.info(" end")

    }

    //Tile(x=3635, y=3362, floor=0)
    //Tile(x=3633, y=3359, floor=0)

    @Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        logger.info("$e")
    }

    @Subscribe
    fun onMsg(e: MessageEvent) {
        logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
    }

    @Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        if (!painter.paintBuilder.trackingInventoryItem(evt.itemId)) {
//            painter.paintBuilder.trackInventoryItem(evt)
        }
    }

    @Subscribe
    fun onNpcAnimation(e: NpcAnimationChangedEvent) {
        val npc = e.npc
        if (npc.healthPercent() < 8 && npc.healthBarVisible()) {
            logger.info("DeadAnim=${e.animation}")
        }
    }

}

class TestPainter(script: TestScript) : ATPaint<TestScript>(script) {

    fun combatWidget(): Widget? {
        return Widgets.stream().firstOrNull { it.components().any { c -> c.text() == "Bloodveld" } }
    }


    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .addString("SelectedInventoryItem") {
                Inventory.selectedItemIndex().toString()
            }
            .addString("SelectedSpell") {
                Magic.magicspell().toString()
            }
            .build()
    }

    override fun paintCustom(g: Rendering) {
        g.setColor(Color.RED)
        val cupboards = script.cupboards
        cupboards.forEachIndexed { i, it -> it.tile.drawOnScreen(text = i.toString()) }
    }

    fun Tile.toWorld(): Tile {
        val a = Game.mapOffset()
        return this.derive(+a.x(), +a.y())
    }

}

fun main() {
    TestScript().startScript("127.0.0.1", "GIM", true)
}
