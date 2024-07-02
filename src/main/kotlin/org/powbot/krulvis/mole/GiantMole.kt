package org.powbot.krulvis.mole

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.teleports.Teleport
import org.powbot.krulvis.api.teleports.TeleportMethod
import org.powbot.krulvis.api.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.teleports.poh.openable.FALADOR_TELEPORT_NEXUS
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.mole.tree.branch.ShouldBank
import kotlin.math.max

@ScriptManifest(name = "krul GiantMole", description = "Dharok that mole back in the ground", version = "1.0.2", category = ScriptCategory.Combat, priv = true, scriptId = "8552ff64-5ae0-4be9-b94b-b580540d8a26")
@ScriptConfiguration.List([
	ScriptConfiguration("Inventory", "What to bring in inventory?", OptionType.INVENTORY),
	ScriptConfiguration("Equipment", "What to bring in equipment?", OptionType.EQUIPMENT),
	ScriptConfiguration("TeleportToBank", "What teleport to use to bank?", OptionType.STRING,
		allowedValues = ["NONE", EDGEVILLE_MOUNTED_GLORY]),
	ScriptConfiguration("TeleportToMole", "What teleport to use to Falador?", OptionType.STRING,
		allowedValues = ["NONE", FALADOR_TELEPORT_NEXUS]),
])
class GiantMole : ATScript() {
	override fun createPainter(): ATPaint<*> = GMPainter(this)
	val rapidHealTimer = Timer(30000)

	val inventory by lazy { getOption<Map<Int, Int>>("Inventory") }
	val prayerPotion by lazy { inventory.keys.mapNotNull { Potion.forId(it) }.firstOrNull { it.skill == Constants.SKILLS_PRAYER } }
	val equipment by lazy { getOption<Map<Int, Int>>("Equipment") }
	val teleportToMole by lazy { TeleportMethod(Teleport.forName(getOption("TeleportToMole"))) }
	val teleportToBank by lazy { TeleportMethod(Teleport.forName(getOption("TeleportToBank"))) }
	override val rootComponent: TreeComponent<*> = ShouldBank(this)
	var lootWatcher: LootWatcher? = null
	val lootList: MutableList<GroundItem> = mutableListOf()
	val moleItems = listOf("Mole skin", "Mole claw")
	val moleBurrowAnimations = intArrayOf(3314, 3315)
	var moleSplatCycles: IntArray = intArrayOf()
	val respawnTimer = Timer(11000)
	val burrowTimer = Timer(5000)

	fun findMole() = Npcs.stream().name("Giant Mole").notAnimation(*moleBurrowAnimations).first()

	private fun GroundItem.isLoot(): Boolean {
		if (name() in moleItems) {
			return true
		}
		return (max(price(), GrandExchange.getItemPrice(id())) * stackSize()) > 1000
	}

	fun disablePrayers() {
		val activePrayers = Prayer.activePrayers()
		activePrayers.forEach { Prayer.prayer(it, false) }
		if (activePrayers.isNotEmpty())
			sleep(600)
	}

	@Subscribe
	fun onNpcAnimation(npcAnim: NpcAnimationChangedEvent) {
		val npc = npcAnim.npc
		if (npc.name() == "Giant Mole") {
			val anim = npcAnim.animation
			logger.info("Mole animation=${anim}")
			if (anim in moleBurrowAnimations) {
				burrowTimer.reset()
			}
			if (npc.healthPercent() == 0 && lootWatcher?.active != true) {
				lootWatcher = LootWatcher(npc.tile(), -1, 5, lootList, isLoot = { it.isLoot() })
				respawnTimer.reset()
			}
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

	@Subscribe
	fun onGameTick(tick: TickEvent) {
		val mole = Npcs.stream().name("Giant Mole").first()
		moleSplatCycles = mole.hitsplatCycles()

	}
}

fun main() {
	GiantMole().startScript("127.0.0.1", "GIM", false)
}