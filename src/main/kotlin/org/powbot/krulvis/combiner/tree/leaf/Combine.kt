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
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner
import org.powbot.krulvis.combiner.interactions.TabInteraction.getTabOpening
import z.con
import kotlin.random.Random

class Combine(script: Combiner) : Leaf<Combiner>(script, "Start combining") {
    override fun execute() {
        Bank.close()
        for (i in 0 until script.combineActions.size) {
            val event = script.combineActions[i]

            val useMenu = !event.rawEntityName.contains("->")
            val prev = if (i > 0) script.combineActions[i - 1] else null
            val next = if (script.combineActions.size > i + 1) script.combineActions[i + 1] else null
            if (next is WidgetActionEvent && next.widget().visible() && next.widget().actions()
                    .contains(next.interaction)
            ) {
                script.logger.info("Next widget=$next is already visible")
                continue
            }
            val interaction = when (event) {
                is InventoryItemActionEvent -> {
                    val item = event.getItem()
                    debug("Event item=${item}")
                    Inventory.open() && item
                        ?.interact(event.interaction, useMenu)
                            ?: false
                }

                is GameObjectActionEvent -> {
                    val obj = Objects.stream().name(event.name).action(event.interaction).nearest().firstOrNull()
                    if (obj == null) {
                        script.logger.info("Cannot find gameobject with name=${event.name} and action=${event.interaction}")
                        Movement.walkTo(event.tile)
                    }
                    Utils.walkAndInteract(
                        Objects.stream().name(event.name).action(event.interaction).firstOrNull(),
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
                    val tabOpening = event.getTabOpening()
                    if (tabOpening != null) {
                        Game.tab(tabOpening)
                    } else if (event.isHomeTeleport() && House.isInside()) {
                        true
                    } else
                        event.widget().interact(event.interaction, false)
                }

                else -> {
                    script.logger.info("None of the handled types: $event")
                    false
                }
            }
            if (next is WidgetActionEvent && next.widget().visible() || interaction) {
                script.logger.info("Interaction for event=$event successfull, next=$next")
                if (next == null) {
                    script.logger.info("Waiting for items to be made...")
                    waitFor(long()) { script.spamClick || !script.stoppedUsing() }
                    if (event is WidgetActionEvent && event.interaction == "Cast" && script.shouldBank()) {
                        script.logger.info("Casting spell as last action")
                        val randomSleep = Random.nextInt(600, 1200)
                        sleep(randomSleep)
                        script.logger.info("Slept for $randomSleep")
                    }
                } else {
                    val wait = waitFor(long()) {
                        if (next.name.contains("->")) {
                            Inventory.selectedItem().id == event.id
                        } else {
                            when (next) {
                                is WidgetActionEvent -> {
                                    if (next.isHomeTeleport()) {
                                        House.isInside()
                                    } else {
                                        val nextWidget = next.widget()
                                        nextWidget.visible() && nextWidget.actions().contains(next.interaction)
                                    }
                                }

                                else -> {
                                    sleep(600)
                                    true
                                }
                            }
                        }
                    }
                    script.logger.info("waitFor next=$next ${if (wait) "success" else "failed"}")
                }
            } else {
                script.logger.info("FAILED interaction for event=$event, next=$next")
            }
        }
    }

    private fun WidgetActionEvent.isHomeTeleport(): Boolean {
        return name == "Teleport to House" && interaction == "Cast"
    }

    private fun InventoryItemActionEvent.getItem(): Item? {
        val itemName = if (name.contains("->")) name.substring(name.indexOf("-> ") + 3) else name
        return Inventory.stream().name(itemName).firstOrNull()
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