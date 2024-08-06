package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.me
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
        logger.info("$e")
    }

    @com.google.common.eventbus.Subscribe
    fun onMsg(e: MessageEvent) {
        logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
    }

    @Subscribe
    fun onProject(e: ProjectileDestinationChangedEvent){
        logger.info("ProjectileEvent id=${e.id}, tile=${e.target()}, destination=${e.destination()}")
    }

    @Subscribe
    fun onTick(e: TickEvent) {
        val tile = me.tile()
        val projectile = Projectiles.stream().filtered { it.tile() == tile }.first()
        if(projectile.valid()){
            logger.info("Found projectile going towards me")
            logger.info("Projectile: id=${projectile.id}")
        }
    }

    override fun poll() {
//        logger.info("Last loop at: $lastLoop was ${System.currentTimeMillis() - lastLoop}ms ago")
        var obj: GameObject? = null
        val timeToFindObj = measureTimeMillis {
            obj = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Portal").first()
        }
//        logger.info("Found portal in $timeToFindObj ms")
        lastLoop = System.currentTimeMillis()
    }

}

fun main() {
    LoopScript().startScript("127.0.0.1", "GIM", true)
}
