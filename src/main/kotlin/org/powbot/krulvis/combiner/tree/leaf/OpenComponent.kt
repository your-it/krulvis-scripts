package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner

class OpenComponent(script: Combiner) : Leaf<Combiner>(script, "Open Component") {
    override fun execute() {
        if (Bank.opened()) {
            Bank.close()
        } else {

            val gameObjectAction =
                script.openWidgetActionEvents.firstOrNull { it is GameObjectActionEvent } as GameObjectActionEvent?
            if (gameObjectAction != null && gameObjectAction.tile != Tile.Nil && gameObjectAction.tile.distance() > 6) {
                Movement.walkTo(gameObjectAction.tile)
            }

            script.openWidgetActionEvents.forEachIndexed { i, event ->
                val interaction = when (event) {
                    is InventoryItemActionEvent -> {
                        Inventory.open()
                        Inventory.stream().id(event.id).firstOrNull()
                            ?.interact(event.interaction, !event.rawEntityName.contains("->"))
                            ?: false
                    }
                    is GameObjectActionEvent -> {
                        Utils.walkAndInteract(
                            Objects.stream().within(event.tile, 2.5).name(event.name).firstOrNull(),
                            event.interaction
                        )
                    }
                    else -> false
                }
                if (interaction && i == script.openWidgetActionEvents.size - 1) {
                    waitFor(long()) { script.combineWidgetActionEvent?.widget()?.visible() == true }
                } else {
                    sleep(605)
                }
            }
        }
    }

}