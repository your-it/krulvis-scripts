package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.event.WidgetActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.selectors.GameObjectOption
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner

class Combine(script: Combiner) : Leaf<Combiner>(script, "Start combining") {
    override fun execute() {
//        val gameObjectAction =
//            script.openWidgetActionEvents.firstOrNull { it is GameObjectActionEvent } as GameObjectActionEvent?
//        if (gameObjectAction != null && gameObjectAction.tile != Tile.Nil && gameObjectAction.tile.distance() > 6) {
//            Movement.walkTo(gameObjectAction.tile)
//        }

        script.combineActions.forEachIndexed { i, event ->
            val interaction = when (event) {
                is InventoryItemActionEvent -> {
                    Inventory.open()
                    event.item()
                        ?.interact(event.interaction, !event.rawEntityName.contains("->"))
                        ?: false
                }
                is GameObjectActionEvent -> {
                    Utils.walkAndInteract(
                        Objects.stream().within(event.tile, 2.5).name(event.name).firstOrNull(),
                        event.interaction
                    )
                }
                is WidgetActionEvent -> {
                    event.widget().interact(event.interaction)
                }
                else -> {
                    script.log.info("None of the handled types: $event")
                    false
                }
            }
            val next =
                if (script.combineActions.size - 1 > i) script.combineActions[i + 1] else null
            if (interaction) {
                script.log.info("Intereraction for event=$event successfull, next=$next")
                if (next == null) {
                    waitFor(long()) { !script.stoppedUsing() }
                } else {
                    waitFor(long()) {
                        when (next) {
                            is InventoryItemActionEvent -> {
                                !next.name.contains("->") || Inventory.selectedItem() != Item.Nil
                            }
                            is WidgetActionEvent -> {
                                next.widget().visible()
                            }
                            else -> {
                                sleep(600)
                                true
                            }
                        }
                    }
                }
            } else {
                return
            }
        }
    }

}