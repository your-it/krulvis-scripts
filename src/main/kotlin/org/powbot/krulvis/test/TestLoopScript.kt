package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
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
