package org.powbot.krulvis.test

import org.powbot.api.*
import org.powbot.api.Condition.wait
import org.powbot.api.action.ActionWaiter
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.VarpbitChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.Walking
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
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
    var newDest = Tile(x = 3094, y = 3491, floor = 0)
    var path: LocalPath = LocalPath(emptyList())

    override val rootComponent: TreeComponent<*> = SimpleLeaf(this, "TestLeaf") {
        val ruins = Objects.stream(25).name("Mysterious ruins").firstOrNull()
        log.info("Found portal: $ruins, distance=${ruins?.distance()}")
        get(25)
    }

    fun get(radius: Int) {
        val ourPosition = Players.local()
        log.info("Local[${ourPosition.localX()}, ${ourPosition.localY()}]")
        log.info("LocalSHR[${ourPosition.localX() shr 7}, ${ourPosition.localY() shr 7}]")
        var minX = (ourPosition.localX() shr 7) - radius
        var minY = (ourPosition.localY() shr 7) - radius
        if (minX < 0 || minY < 0 || minX > 103 || minY > 103) {
            minX = 0
            minY = 0
        }
        var maxX = (ourPosition.localX() shr 7) + radius
        var maxY = (ourPosition.localY() shr 7) + radius
        if (maxX <= 0 || maxY < 0 || maxX > 103 || maxY > 103) {

            maxX = 103
            maxY = 103
        }
        log.info("min[$minX, $minY], max[$maxX, $maxY]")
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
