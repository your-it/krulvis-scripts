package org.powbot.krulvis.test

import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.rt4.magic.RunePouch.RUNE_AMOUNT_MASK
import org.powbot.api.rt4.magic.RunePouch.RUNE_ID_MASK
import org.powbot.api.rt4.magic.RunePouch.RUNE_POUCH_VARP
import org.powbot.api.rt4.magic.RunePouch.RUNE_POUCH_VARP2
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.*
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.BotManager
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.drawing.Rendering
import org.powerbot.bot.rt4.client.internal.IClient
import org.powerbot.bot.rt4.client.internal.ICombatStatusData
import kotlin.math.ceil
import kotlin.math.pow
import kotlin.system.measureTimeMillis

@ScriptManifest(name = "Krul Test Loop", version = "1.0.1", description = "", priv = true)
class LoopScript : AbstractScript() {

    var origin = Tile(3212, 3216, 0) //varrock mine

    var collisionMap: Array<IntArray> = emptyArray()

    //    val dest = Tile(3253, 3420, 0) //Varrock bank
    var newDest = Tile(3231, 3207, 0)
    var localPath: LocalPath = LocalPath(emptyList())
    var comp: Component? = null
    val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks") }
    var path = emptyList<Edge<*>?>()
    var obj: GameObject? = null
    var npc: Npc? = null
    var lastLoop = System.currentTimeMillis()

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
    }

    override fun poll() {
        log.info("Last loop at: $lastLoop was ${System.currentTimeMillis() - lastLoop}ms ago")
        var obj: GameObject? = null
        val timeToFindObj = measureTimeMillis {
            obj = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Portal").first()
        }
        log.info("Found portal in $timeToFindObj ms")
        lastLoop = System.currentTimeMillis()
    }

}

fun main() {
    LoopScript().startScript("127.0.0.1", "GIM", true)
}
