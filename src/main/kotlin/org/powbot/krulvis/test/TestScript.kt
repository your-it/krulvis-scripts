package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.VarpbitChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(name = "Krul TestScriptu", version = "1.0.1", description = "")
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
    override fun createPainter(): ATPaint<*> = TestPainter(this)

    val origin = Tile(3051, 4858, 0) //varrock mine

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3048, 4811, 0)
    var path: LocalPath = LocalPath(emptyList())

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        path = LocalPathFinder.findPath(origin, newDest)
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

class TestPainter(script: TestScript) : ATPaint<TestScript>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
        script.origin.drawOnScreen(g)
        script.newDest.drawOnScreen(g)
        script.path.draw(g)
    }

}

fun main() {
    TestScript().startScript(true)
}
