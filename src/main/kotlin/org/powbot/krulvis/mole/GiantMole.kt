package org.powbot.krulvis.mole

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.MessageType
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.GrandExchange
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.mole.tree.branch.ShouldBank
import kotlin.math.max

@ScriptManifest(name = "krul GiantMole", description = "1.0.0", category = ScriptCategory.Combat, priv = true)
class GiantMole : ATScript() {
    override fun createPainter(): ATPaint<*> = GMPainter(this)
    val rapidHealTimer = Timer(30000)

    val inventory by lazy { getOption<Map<Int, Int>>("Inventory") }
    val prayerPotion by lazy { inventory.keys.mapNotNull { Potion.forId(it) }.firstOrNull { it.skill == Constants.SKILLS_PRAYER } }
    val equipment by lazy { getOption<Map<Int, Int>>("Equipment") }
    fun findMole() = Npcs.stream().name("Giant Mole").first()
    override val rootComponent: TreeComponent<*> = ShouldBank(this)
    var lootWatcher = LootWatcher(Tile.Nil, -1, lootList = mutableListOf(), isLoot = { false })
    val lootList: MutableList<GroundItem> = mutableListOf()
    val moleItems = listOf("Mole skin", "Mole claw")

    private fun GroundItem.isLoot(): Boolean {
        if (name() in moleItems) {
            return true
        }
        return (max(price(), GrandExchange.getItemPrice(id())) * stackSize()) > 1000
    }

    @Subscribe
    fun onNpcAnimation(npoAnim: NpcAnimationChangedEvent) {
        val npc = npoAnim.npc
        if (npc.name() == "Giant Mole" && npc.healthPercent() == 0 && !lootWatcher.active) {
            lootWatcher = LootWatcher(npc.tile(), -1, 5, lootList, isLoot = { it.isLoot() })
        }
    }

    @Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        val pot = Potion.forId(evt.itemId)
        if (evt.quantityChange > 0 && id != VIAL
            && !inventory.containsKey(id) && !equipment.containsKey(id)
            && pot == null
        ) {
            painter.trackItem(id, evt.quantityChange)
        }
    }

    @Subscribe
    fun messageReceived(msg: MessageEvent) {
        if (msg.messageType != MessageType.Game) return
        if (msg.message.contains("so you can't take ")) {
            logger.info("Ironman message CANT TAKE type=${msg.messageType}")
            lootList.clear()
        }
    }
}

fun main() {
    GiantMole().startScript("127.0.0.1", "GIM", false)
}