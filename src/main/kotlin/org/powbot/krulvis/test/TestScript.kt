package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.*
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.mobile.drawing.Graphics
import kotlin.system.measureTimeMillis

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "")
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "rocks",
            description = "Click som rocks",
            optionType = OptionType.GAMEOBJECTS,
            defaultValue = "[{\"id\":114,\"interaction\":\"Examine\",\"mouseX\":396,\"mouseY\":229,\"rawEntityName\":\"<col=ffff>Cooking range\",\"rawOpcode\":1002,\"var0\":52,\"widgetId\":47,\"name\":\"Cooking range\",\"tile\":{\"floor\":0,\"x\":3212,\"y\":3215,\"rendered\":true},\"strippedName\":\"Cooking range\"},{\"id\":14867,\"interaction\":\"Examine\",\"mouseX\":408,\"mouseY\":259,\"rawEntityName\":\"<col=ffff>Barrel\",\"rawOpcode\":1002,\"var0\":52,\"widgetId\":49,\"name\":\"Barrel\",\"tile\":{\"floor\":0,\"x\":3212,\"y\":3217,\"rendered\":true},\"strippedName\":\"Barrel\"}]"
        ),
        ScriptConfiguration(
            name = "npcs",
            description = "NPCS?",
            optionType = OptionType.NPCS,
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

    var origin = Tile(3215, 3227, 0) //varrock mine

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3224, 3237, 0)
    var path: LocalPath = LocalPath(emptyList())
    var comp: Component? = null


    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {

        sleep(1000)
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
            .trackInventoryItem(12012)
            .build()
    }

    override fun paintCustom(g: Graphics) {
        script.origin.drawOnScreen(g)
        script.newDest.drawOnScreen(g)
        script.path.draw(g)
        script.comp?.draw(g)
    }

}

fun main() {
    TestScript().startScript(true)
}
