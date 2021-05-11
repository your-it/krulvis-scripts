package org.powbot.krulvis.walking

import org.powbot.walking.model.Skill
import org.powbot.walking.model.SpellBook
import org.powbot.walking.model.WebWalkingPlayer
import org.powerbot.script.Condition
import org.powerbot.script.rt4.*
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicReference

class PlayerState  {

    companion object {
        const val CompleteQuestColor = 901389
        const val QuestsWidgetParentId = 399
        const val F2PQuestsWidgetChildId = 6
        const val P2PQuestsWidgetChildId = 6

        val logger = LoggerFactory.getLogger(PlayerState::class.java)
    }

    var cachedWorlds = AtomicReference<List<WorldListItem>?>(null)
    var cachedQuests = AtomicReference<Set<String>>()
    var cachedUsername = AtomicReference<String?>(null)

    private fun getCompletedQuests(parent: Int, child: Int): Set<String> {
        val component = ClientContext.ctx().widgets.component(parent, child)
        if (component == null || !component.valid() || component.components().isEmpty()) {
            if (ClientContext.ctx().game.tab(Game.Tab.QUESTS)) {
                return (getCompletedQuests(parent, child))
            }
        }

        return component.components().filterNotNull()
            .filter { it.valid() && it.textColor() == CompleteQuestColor }
            .mapNotNull { it.text() }
            .toSet()
    }

    private fun getInventory(): Map<String, Int> {
        val items = ClientContext.ctx().inventory.items()
            .filterNotNull().filter { it.id() > 0 }
            .groupBy { it.name() }
            .map { it.key to it.value.map { item -> item.stackSize() }.sum() }.toMap()
        if (items.isEmpty() && !ClientContext.ctx().game.tab(Game.Tab.INVENTORY)) {
            if (ClientContext.ctx().game.tab(Game.Tab.INVENTORY)) {
                return getInventory()
            }
        }

        return items
    }

    private fun getEquipment(): Map<String, Int> {
        val items = ClientContext.ctx().equipment.get()
            .filterNotNull().filter { it.id() > 0 }
            .groupBy { it.name() }
            .map { it.key to it.value.map { item -> item.stackSize() }.sum() }.toMap()
        if (items.isEmpty() && !ClientContext.ctx().game.tab(Game.Tab.EQUIPMENT)) {
            if (ClientContext.ctx().game.tab(Game.Tab.EQUIPMENT)) {
                return getEquipment()
            }
        }

        return items
    }

    private fun getWorlds(): List<WorldListItem>? {
        return WorldList.getWorlds()
    }

    fun player(refreshQuests: Boolean): WebWalkingPlayer? {
        synchronized(this) {
            if (!ClientContext.ctx().game.loggedIn()) {
                return null
            }
            val quests = if (refreshQuests || ClientContext.ctx().client().username != cachedUsername.get()) {
                val f2pQuests = getCompletedQuests(QuestsWidgetParentId, F2PQuestsWidgetChildId)
                val p2pQuests = getCompletedQuests(QuestsWidgetParentId, P2PQuestsWidgetChildId)
                val quests = setOf(*f2pQuests.toTypedArray(), *p2pQuests.toTypedArray())

                cachedUsername.set(ClientContext.ctx().client().username)
                cachedQuests.set(quests)

                quests
            } else {
                cachedQuests.get()
            }

            val inventory = getInventory()
            val equipment = getEquipment()

            val worlds = if (cachedWorlds.get() != null) {
                cachedWorlds.get()
            } else {
                val tmpWorlds = getWorlds()
                cachedWorlds.set(tmpWorlds)

                tmpWorlds
            }

            val world = worlds?.firstOrNull() { it.number == ClientContext.ctx().client().currentWorld - 300 }

            return WebWalkingPlayer(
                location = ClientContext.ctx().players.local().tile().toWebTile(),
                skillLevels = ClientContext.ctx().skills.levels()
                    .mapIndexed { idx, level -> Skill.values()[idx] to level }.toMap(),
                completedQuests = quests.toSet(),
                inventory = inventory,
                equipment = equipment,
                canHomeTeleport = false, //TODO
                wildernessLevel = 0,//TODO
                spellBook = SpellBook.Regular,
                members = world?.members == true
            )
        }
    }
}
