package org.powbot.krulvis.fighter

import com.google.common.eventbus.Subscribe
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldEat
import org.powbot.krulvis.api.teleports.*
import org.powbot.krulvis.api.teleports.poh.LUNAR_ISLE_HOUSE_PORTAL
import org.powbot.krulvis.api.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.teleports.poh.openable.FEROX_ENCLAVE_JEWELLERY_BOX
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.fighter.Defender.currentDefenderIndex
import org.powbot.krulvis.fighter.tree.branch.ShouldStop
import org.powbot.mobile.rscache.loader.ItemLoader
import kotlin.math.round


//<editor-fold desc="ScriptManifest">
@ScriptManifest(
	name = "krul Fighter",
	description = "Fights anything, anywhere. Supports defender collecting.",
	author = "Krulvis",
	version = "1.4.6",
	markdownFileName = "Fighter.md",
	scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
	category = ScriptCategory.Combat,
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			WARRIOR_GUILD_OPTION, "Collect defenders in the warrior guild",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			"Inventory", "What should your inventory look like?",
			optionType = OptionType.INVENTORY
		),
		ScriptConfiguration(
			"Equipment", "What do you want to wear?",
			optionType = OptionType.EQUIPMENT
		),
		ScriptConfiguration(
			"Monsters",
			"Click the NPC's you want to kill",
			optionType = OptionType.NPC_ACTIONS
		),
		ScriptConfiguration(
			"Radius", "Kill radius", optionType = OptionType.INTEGER, defaultValue = "10"
		),
		ScriptConfiguration(
			"Use safespot", "Do you want to force a safespot?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			"Walk back",
			"Walk to safespot after attacking?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "false",
			visible = false
		),
		ScriptConfiguration(
			"Safespot", "Get safespot / centertile",
			optionType = OptionType.TILE
		),
		ScriptConfiguration(
			"WaitForLoot", "Wait for loot after kill?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			"Ironman", description = "Only pick up your own drops.",
			optionType = OptionType.BOOLEAN, defaultValue = "true"
		),
		ScriptConfiguration(
			"Loot price", "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
		),
		ScriptConfiguration(
			"Always loot",
			"Separate items with \",\" Start with \"!\" to never loot",
			optionType = OptionType.STRING,
			defaultValue = "Long bone, curved bone, clue, totem, !blue dragon scale, Scaly blue dragonhide, toadflax, irit, avantoe, kwuarm, snapdragon, cadantine, lantadyme, dwarf weed, torstol"
		),
		ScriptConfiguration(
			"Bury bones", "Bury, Scatter or Offer bones.",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			"BankTeleport", "Teleport to bank", optionType = OptionType.STRING, defaultValue = EDGEVILLE_MOUNTED_GLORY,
			allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX]
		),
		ScriptConfiguration(
			"NpcTeleport", "Teleport to NPCs", optionType = OptionType.STRING, defaultValue = EDGEVILLE_MOUNTED_GLORY,
			allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX, LUNAR_ISLE_HOUSE_PORTAL]
		)
	]
)
//</editor-fold>
class Fighter : ATScript() {

	override fun createPainter(): ATPaint<*> = FighterPainter(this)

	override val rootComponent: TreeComponent<*> = ShouldEat(this, ShouldStop(this))
	override fun onStart() {
		super.onStart()
		Defender.lastDefenderIndex = currentDefenderIndex()
	}

	//<editor-fold desc="UISubscribers">
	@ValueChanged(WARRIOR_GUILD_OPTION)
	fun onWGChange(inWG: Boolean) {
		if (inWG) {
			updateOption("Safespot", Defender.killSpot(), OptionType.TILE)
			val npcAction = NpcActionEvent(
				0, 0, 10, 13729, 0,
				"Attack", "<col=ffff00>Cyclops<col=40ff00>  (level-106)",
				447, 447, -1
			)
			updateOption("Monsters", listOf(npcAction), OptionType.NPC_ACTIONS)
			updateOption("Radius", 25, OptionType.INTEGER)
			updateOption("Bank", "WARRIORS_GUILD", OptionType.STRING)
		}
	}

	@ValueChanged("Use safespot")
	fun onSafeSpotChange(useSafespot: Boolean) {
		updateVisibility("Walk back", useSafespot)
	}


	//</editor-fold desc="Configuration">


	//Warrior guild
	val warriorTokens = 8851
	val warriorGuild by lazy { getOption<Boolean>(WARRIOR_GUILD_OPTION) }

	//Safespot options
	val useSafespot by lazy { getOption<Boolean>("Use safespot") }
	val walkBack by lazy { getOption<Boolean>("Walk back") }
	private val safespot by lazy { getOption<Tile>("Safespot") }
	val buryBones by lazy { getOption<Boolean>("Bury bones") }

	//Inventory
	private val inventoryOptions by lazy { getOption<Map<Int, Int>>("Inventory") }
	val requiredInventory by lazy { inventoryOptions.filterNot { Potion.isPotion(it.key) } }
	val requiredPotions by lazy {
		inventoryOptions.filter { Potion.isPotion(it.key) }
			.mapNotNull { Pair(Potion.forId(it.key), it.value) }
			.groupBy {
				it.first
			}.map { it.key!! to it.value.sumOf { pair -> pair.second } }
	}
	val hasPrayPots by lazy { requiredPotions.any { it.first.skill == Constants.SKILLS_PRAYER } }

	//Equipment
	private val equipmentOptions by lazy { getOption<Map<Int, Int>>("Equipment") }
	val equipment by lazy {
		equipmentOptions.filterNot { TeleportItem.isTeleportItem(it.key) }.map {
			Equipment(
				Slot.forIndex(it.value),
				it.key
			)
		}
	}
	val teleportItems by lazy {
		equipmentOptions.keys.mapNotNull {
			TeleportItem.getTeleportItem(it)
		}
	}

	//Banking option
	var forcedBanking = false
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption("BankTeleport"))) }

	//Killing spot
	val monsters by lazy {
		getOption<List<NpcActionEvent>>("Monsters").map { it.name }
	}
	val radius by lazy { getOption<Int>("Radius") }
	val waitForLootAfterKill by lazy { getOption<Boolean>("WaitForLoot") }
	val npcTeleport by lazy { TeleportMethod(Teleport.forName(getOption("NpcTeleport"))) }
	var currentTarget: Npc? = null
	val aggressionTimer = Timer(10 * 60 * 1000)


	//Loot
	var waitingForLootTile: Tile? = null
	fun isWaitingForLoot() = waitForLootJob?.isActive == true
	var waitForLootJob: Job? = null
	val lootList = mutableListOf<GroundItem>()
	val ironman by lazy { getOption<Boolean>("Ironman") }
	val minLootPrice by lazy { getOption<Int>("Loot price") }
	val lootNameOptions by lazy {
		val names = getOption<String>("Always loot").split(",")
		val trimmed = mutableListOf<String>()
		names.forEach { trimmed.add(it.trim().lowercase()) }
		trimmed
	}
	var ammoId: Int = -1
	val lootNames by lazy {
		val names = lootNameOptions.filterNot { it.startsWith("!") }.toMutableList()
		val ammo = equipment.firstOrNull { it.slot == Slot.QUIVER }
		if (ammo != null) {
			ammoId = ammo.id
			names.add(ItemLoader.lookup(ammoId)?.name()?.lowercase() ?: "nulll")
		}
		names.add("brimstone key")
		names.add("ancient shard")
		logger.info("Looting: [${names.joinToString()}]")
		names.toList()
	}
	val neverLoot by lazy {
		val trimmed = mutableListOf<String>()
		lootNameOptions
			.filter { it.startsWith("!") }
			.forEach { trimmed.add(it.replace("!", "")) }
		logger.info("Not looting: [${trimmed.joinToString()}]")
		trimmed
	}


	fun centerTile() = safespot

	fun shouldReturnToSafespot() =
		useSafespot && centerTile() != Players.local().tile() && (walkBack || Players.local().healthBarVisible())

	fun nearbyMonsters(): List<Npc> =
		Npcs.stream().within(centerTile(), radius.toDouble()).name(*monsters.toTypedArray()).nearest().list()


	fun target(): Npc? {
		val local = Players.local()
		val nearbyMonsters =
			nearbyMonsters().filterNot { it.healthBarVisible() && (it.interacting() != local || it.healthPercent() == 0) }
		val attackingMe = nearbyMonsters.firstOrNull { it.interacting() == local && it.reachable() }
		return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() }
	}

	fun taskRemainder() = Varpbits.varpbit(394)


	fun watchLootDrop(tile: Tile) {
		if (waitForLootJob?.isActive != true) {
			logger.info("Waiting for loot at $tile")
			val startMilis = System.currentTimeMillis()
			waitingForLootTile = tile
			waitForLootJob = GlobalScope.launch {
				val watcher = LootWatcher(tile, ammoId, isLoot = { it.isLoot() })
				val loot = watcher.waitForLoot()
				logger.info("Waiting for loot took: ${round((System.currentTimeMillis() - startMilis) / 100.0) / 10.0} seconds")
				lootList.addAll(loot)
				watcher.unregister()
				waitingForLootTile = null
			}
		} else {
			logger.info("Already watching loot at tile: $tile for loot")
		}
	}

	fun GroundItem.isLoot(): Boolean {
		if (warriorGuild && id() in Defender.defenders) return true
		val name = name().lowercase()
		return !neverLoot.contains(name) &&
			(lootNames.any { ln -> name.contains(ln) } || getPrice() * stackSize() >= minLootPrice)
	}


	fun loot(): List<GroundItem> =
		if (ironman) lootList else GroundItems.stream().within(centerTile(), radius).filter { it.isLoot() }

	var npcDeathWatchers: MutableList<NpcDeathWatcher> = mutableListOf()

	@Subscribe
	fun onTickEvent(_e: TickEvent) {
		val interacting = me.interacting()
		if (interacting is Npc && interacting != Npc.Nil) {
			currentTarget = interacting
			val watcher = npcDeathWatchers.firstOrNull { it.npc == interacting }
			if (watcher == null || !watcher.active) {
				npcDeathWatchers.add(NpcDeathWatcher(interacting) { watchLootDrop(interacting.tile()) })
			}
		}
		npcDeathWatchers.removeAll { !it.active }
	}

	@Subscribe
	fun onInventoryChange(evt: InventoryChangeEvent) {
		val id = evt.itemId
		val pot = Potion.forId(evt.itemId)
		val isTeleport = TeleportItem.isTeleportItem(id)
		if (evt.quantityChange > 0 && id != VIAL
			&& id !in Defender.defenders
			&& !requiredInventory.containsKey(id) && !equipmentOptions.containsKey(id)
			&& !isTeleport && requiredPotions.none { it.first == pot }
		) {
			if (painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
				painter.paintBuilder.trackInventoryItems(id)
				logger.info("Now tracking: ${ItemLoader.lookup(id)?.name()} adding ${evt.quantityChange} as start")
				painter.paintBuilder.items.forEach { row ->
					val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
					if (item != null) (item as InventoryItemPaintItem).diff += evt.quantityChange
				}
			}
		}
	}

	@Subscribe
	fun messageReceived(msg: MessageEvent) {
		if (msg.message.contains("so you can't take ")) {
			logger.info("Ironman message CANT TAKE type=${msg.messageType}")
			lootList.clear()
		}
		if (msg.message.contains("A superior foe has appeared")) {
			logger.info("Superior appeared message received: type=${msg.messageType}")
			superiorAppeared = true
		}
	}


	//Custom slayer options
	var lastTask = false
	var superiorAppeared = false

	@Subscribe
	fun onPaintCheckbox(pcce: PaintCheckboxChangedEvent) {
		if (pcce.checkboxId == "stopAfterTask") {
			lastTask = pcce.checked
			val painter = painter as FighterPainter
			if (pcce.checked && !painter.paintBuilder.items.contains(painter.slayerTracker)) {
				val index =
					painter.paintBuilder.items.indexOfFirst { row -> row.any { it is CheckboxPaintItem && it.id == "stopAfterTask" } }
				painter.paintBuilder.items.add(index, painter.slayerTracker)
			} else if (!pcce.checked && painter.paintBuilder.items.contains(painter.slayerTracker)) {
				painter.paintBuilder.items.remove(painter.slayerTracker)
			}
		}
	}
}


fun main() {
	Fighter().startScript("127.0.0.1", "GIM", false)
}