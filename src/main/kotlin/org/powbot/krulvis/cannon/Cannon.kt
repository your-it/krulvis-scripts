package org.powbot.krulvis.cannon

import org.powbot.api.event.MessageEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Projectiles
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldEat
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion

@ScriptManifest(
    name = "krul Cannon",
    description = "Refills cannon, eats food and drinks pray pots",
    version = "1.0.0",
    priv = true,
    author = "Krulvis"
)
class Cannon : ATScript() {

    override fun createPainter(): ATPaint<*> {
        return CannonPainter(this)
    }

    override val rootComponent: TreeComponent<*> = ShouldEat(this, ShouldSipPotion(this, ShouldRepair(this)))

    var ballsLeft = 0

    fun cannon() = Objects.stream().name("Cannon").firstOrNull()

    @com.google.common.eventbus.Subscribe
    fun onMessage(e: MessageEvent) {
        if (e.message == "You refill the cannon.") {
            ballsLeft = 30
        } else if (e.message == "Your cannon is out of ammo.") {
            ballsLeft = 0
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(e: TickEvent) {
        val projectiles = Projectiles.stream().list()
        log.info("Proj=[${projectiles.joinToString { "cycle=${it.cycleStart}, ${it.id}" }}]")
    }

}

class CannonPainter(script: Cannon) : ATPaint<Cannon>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackSkill(Skill.Ranged)
            .addString("Balls left: ") { script.ballsLeft.toString() }
            .build()
    }
}

fun main() {
    Cannon().startScript()
}