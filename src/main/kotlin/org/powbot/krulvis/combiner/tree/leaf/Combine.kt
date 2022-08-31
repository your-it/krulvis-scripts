package org.powbot.krulvis.combiner.tree.leaf

import org.powbot.api.InteractableEntity
import org.powbot.api.Locatable
import org.powbot.api.Nameable
import org.powbot.api.Tile
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.event.WidgetActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.rt4.walking.local.Utils.turnRunOn
import org.powbot.api.script.selectors.GameObjectOption
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner

class Combine(script: Combiner) : Leaf<Combiner>(script, "Start combining") {
    override fun execute() {
        script.combineActions.forEachIndexed { i, event ->
            val useMenu = !event.rawEntityName.contains("->")
            val prev = if (i > 0) script.combineActions[i - 1] else null
            val next =
                if (script.combineActions.size == i + 1) null else script.combineActions[i + 1]
            if (next is WidgetActionEvent && next.widget().visible()) {
                return@forEachIndexed
            }
            val interaction = when (event) {
                is InventoryItemActionEvent -> {
                    Inventory.open() && event.item()
                        ?.interact(event.interaction, useMenu)
                            ?: false
                }
                is GameObjectActionEvent -> {
                    Utils.walkAndInteract(
                        Objects.stream().within(event.tile, 2.5).name(event.name).firstOrNull(),
                        event.interaction,
                        selectItem = if (prev is InventoryItemActionEvent && prev.interaction == "Use") prev.id else -1
                    )
                }
                is NpcActionEvent -> {
                    Utils.walkAndInteract(
                        Npcs.stream().name(event.name).nearest().firstOrNull(),
                        event.interaction,
                        selectItem = if (prev is InventoryItemActionEvent && prev.interaction == "Use") prev.id else -1
                    )
                }
                is WidgetActionEvent -> {
                    event.widget().interact(event.interaction, false)
                }
                else -> {
                    script.log.info("None of the handled types: $event")
                    false
                }
            }
            if (interaction || (next is WidgetActionEvent && next.widget().visible())) {
                script.log.info("Interaction for event=$event successfull, next=$next")
                if (next == null) {
                    waitFor(long()) { script.spamClick || !script.stoppedUsing() }
                } else {
                    val wait = waitFor(long()) {
                        if (event.name.contains("->")) {
                            Inventory.selectedItem().id == event.id
                        } else {
                            when (next) {
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
                    script.log.info("waitFor next=$next ${if (wait) "success" else "failed"}")
                }
            } else {
                script.log.info("FAILED interaction for event=$event, next=$next")
                return
            }
        }
    }

    fun walkAndInteract(target: InteractableEntity?, action: String, useMenu: Boolean): Boolean {
        val t = target ?: return false
        val name = (t as Nameable).name()
        val pos = (t as Locatable).tile()
        val destination = Movement.destination()
        turnRunOn()

        //Close opened interfaces
        Bank.close()
        GrandExchange.close()
        DepositBox.close()

        //If not visible or too far away, walk first
        if (!t.inViewport()
            || (destination != pos && pos.distanceTo(if (destination == Tile.Nil) Players.local() else destination) > 12)
        ) {
            Camera.turnTo(target.tile())
            LocalPathFinder.findPath(pos).traverse()
        }
        return t.interact(action, name, useMenu)
    }

}