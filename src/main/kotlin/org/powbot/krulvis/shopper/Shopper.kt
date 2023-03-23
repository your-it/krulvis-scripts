package org.powbot.krulvis.shopper

import org.powbot.api.Random
import org.powbot.api.rt4.Components
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Store
import org.powbot.api.rt4.Widgets
import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.mobile.script.ScriptManager
import org.powbot.proto.rt4.Interaction

@ScriptManifest(
        name = "Shopper",
        author = "Krulvis",
        version = "1.0.0",
        description = "Sells/Buys store items",
        priv = true
)
class Shopper : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return Painter(this)
    }

    override val rootComponent: TreeComponent<*>
        get() = SimpleLeaf(this, "Selling") {
            val chaos = Inventory.stream().id(Rune.CHAOS.id).firstOrNull()
            if (chaos == null) {
                log.info("Can't find chaos rune")
                ScriptManager.stop()
                return@SimpleLeaf
            }
            if (Store.opened()) {
                val index = chaos.inventoryIndex
                val comp = Widgets.component(149, 0, index)
                if (!comp.interact("Sell 50")) {
                    log.info("Failed to interact")
                }
            } else {
                log.info("Store not opened...")
                ScriptManager.stop()
            }
        }
}

fun main() {
    Shopper().startScript("127.0.0.1", "GIM", true)
}