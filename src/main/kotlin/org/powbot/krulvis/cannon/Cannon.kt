package org.powbot.krulvis.cannon

import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.Objects
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldEat
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion

@ScriptManifest(
    name = "krul Cannon",
    description = "Refills cannon, eats food and drinks pray pots",
    version = "1.0.0",
    author = "Krulvis"
)
class Cannon : ATScript() {
    override fun createPainter(): ATPaint<*> {
        TODO("Not yet implemented")
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

}