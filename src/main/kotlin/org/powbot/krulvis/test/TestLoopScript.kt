package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul Test Loop", version = "1.0.1", description = "", priv = true)
class LoopScript : AbstractScript() {

    var collisionMap: Array<IntArray> = emptyArray()
    var npc: Npc = Npc.Nil
    var projectiles = emptyList<Projectile>()


    @com.google.common.eventbus.Subscribe
    fun onGameActionEvent(e: GameActionEvent) {
        logger.info("$e")
        if (e is NpcActionEvent) {
            npc = e.npc()
            logger.info("Interacted with npc=${npc.name()}")
        }
    }

    @Subscribe
    fun onProject(e: ProjectileDestinationChangedEvent) {
        if (e.target() == Actor.Nil) {
            logger.info("ProjectileEvent id=${e.id}, destination=${e.destination()}")
        }
    }

    @Subscribe
    fun onTick(e: TickEvent) {
        val projectiles = Projectiles.stream().filtered { it.target() == Actor.Nil }.toList()
        val npc = Npcs.stream().name("Tormented demon").nearest().first()

        if (projectiles.isNotEmpty()) {
            logger.info("Found (${projectiles.size}) projectiles without target")
        }
    }

    override fun poll() {

        val cannon = Objects.stream().name("Dwarf multicannon").nearest().first()
        logger.info("Cannon=${cannon}")
    }

    fun getCannonballCount(): Int {
        val comp = Components.stream(651, 4).itemId(2).first()
        if (!comp.visible()) return 0
        val parent = comp.parent()
        if (parent.componentCount() <= comp.index() + 2) return 0
        return comp.parent().component(comp.index() + 2).text().toInt()
    }


    @Subscribe
    fun onRender(e: RenderEvent) {
        val g = Rendering
        val x = 10
        var y = 20
        val npc = npc
        if (npc.valid()) {
            g.drawString("Anim=${npc.animation()}, Prayer=${npc.prayerHeadIconId()}", x, y)
        }
        y += 15
    }

}

fun main() {
    LoopScript().startScript("127.0.0.1", "GIM", true)
}
