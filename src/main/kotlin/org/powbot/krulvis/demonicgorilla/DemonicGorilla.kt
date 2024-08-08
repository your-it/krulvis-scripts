package org.powbot.krulvis.demonicgorilla

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.extensions.items.IEquipmentItem
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.teleports.*
import org.powbot.krulvis.api.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.teleports.poh.openable.FEROX_ENCLAVE_JEWELLERY_BOX
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.requirements.EquipmentRequirement
import org.powbot.krulvis.api.utils.requirements.InventoryRequirement
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_DEATH_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_MAGE_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_MELEE_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DEMONIC_GORILLA_RANGE_ANIM
import org.powbot.krulvis.demonicgorilla.Data.DemonicPrayer
import org.powbot.krulvis.demonicgorilla.tree.branch.ShouldStop
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.BURY_BONES_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.mobile.script.ScriptManager
import kotlin.math.floor


//<editor-fold desc="ScriptManifest">
@ScriptManifest(
	name = "krul DemonicGorilla",
	description = "Fights Demonic Gorilla.",
	author = "Krulvis",
	version = "1.0.0",
	category = ScriptCategory.Combat,
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			INVENTORY_OPTION, "What should your inventory look like?",
			optionType = OptionType.INVENTORY
		),
		ScriptConfiguration(
			USE_MELEE_OPTION, "Use melee gear?",
			optionType = OptionType.BOOLEAN, defaultValue = "true", visible = false
		),
		ScriptConfiguration(
			MELEE_EQUIPMENT_OPTION, "What melee gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = true
		),
		ScriptConfiguration(
			MELEE_PRAYER_OPTION, "What melee offensive prayer to use?",
			optionType = OptionType.STRING, visible = false, defaultValue = "PIETY", allowedValues = ["NONE", "CHIVALRY", "PIETY"]
		),
		ScriptConfiguration(
			USE_RANGE_OPTION, "Use ranged gear?",
			optionType = OptionType.BOOLEAN, visible = false
		),
		ScriptConfiguration(
			RANGE_EQUIPMENT_OPTION, "What ranged gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = false
		),
		ScriptConfiguration(
			RANGE_PRAYER_OPTION, "What range offensive prayer to use?",
			optionType = OptionType.STRING, visible = false, defaultValue = "EAGLE_EYE", allowedValues = ["NONE", "SHARP_EYE", "HAWK_EYE", "EAGLE_EYE", "RIGOUR"]
		),
		ScriptConfiguration(
			USE_MAGE_OPTION, "Use mage gear?",
			optionType = OptionType.BOOLEAN, visible = false
		),
		ScriptConfiguration(
			MAGE_EQUIPMENT_OPTION, "What mage gear do you want to use?",
			optionType = OptionType.EQUIPMENT, visible = false
		),
		ScriptConfiguration(
			MAGE_PRAYER_OPTION, "What mage offensive prayer to use?",
			optionType = OptionType.STRING, visible = false, defaultValue = "MYSTIC_MIGHT", allowedValues = ["NONE", "MYSTIC_WILL", "MYSTIC_LORE", "MYSTIC_MIGHT", "AUGURY"]
		),
		ScriptConfiguration(
			BURY_BONES_OPTION, "Scatter ashes?",
			optionType = OptionType.BOOLEAN, defaultValue = "false"
		),
		ScriptConfiguration(
			CASTLE_WARS_JEWELLERY_BOX,
			"Teleport to bank",
			optionType = OptionType.STRING,
			defaultValue = EDGEVILLE_MOUNTED_GLORY,
			allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX]
		),
	]
)
//</editor-fold>
class DemonicGorilla : ATScript() {

	override fun createPainter(): ATPaint<*> = DGPainter(this)

	override val rootComponent: TreeComponent<*> = ShouldStop(this)
	override fun onStart() {
		super.onStart()
		equipment = meleeEquipment
		if (buryBones) lootNames.add("malicious ashes")
	}

	//<editor-fold desc="UISubscribers">
	@ValueChanged(MULTI_STYLE_OPTION)
	fun onMultiStyle(multiStyle: Boolean) {
		updateDescription(MELEE_EQUIPMENT_OPTION, if (multiStyle) "What melee gear do you want to use?" else "What gear do you want to use?")
		updateVisibility(USE_MELEE_OPTION, multiStyle)
		updateVisibility(USE_RANGE_OPTION, multiStyle)
		updateVisibility(USE_MAGE_OPTION, multiStyle)
	}

	@ValueChanged(USE_MELEE_OPTION)
	fun onUseMelee(useMelee: Boolean) {
		updateVisibility(MELEE_EQUIPMENT_OPTION, useMelee)
		updateVisibility(MELEE_PRAYER_OPTION, useMelee)
	}

	@ValueChanged(USE_RANGE_OPTION)
	fun onUseRange(useRange: Boolean) {
		updateVisibility(RANGE_EQUIPMENT_OPTION, useRange)
		updateVisibility(RANGE_PRAYER_OPTION, useRange)
	}

	@ValueChanged(USE_MAGE_OPTION)
	fun onUseMage(useMage: Boolean) {
		updateVisibility(MAGE_EQUIPMENT_OPTION, useMage)
		updateVisibility(MAGE_PRAYER_OPTION, useMage)
	}
	//</editor-fold desc="UISubscribers">


	//Inventory
	private val inventoryOptions by lazy { getOption<Map<Int, Int>>(INVENTORY_OPTION) }
	val requiredInventory by lazy { inventoryOptions.map { InventoryRequirement(it.key, it.value) } }
//	val requiredPotions by lazy {
//		requiredInventory.filter { it.item is Potion }.map { PotionRequirement(it.item as Potion, it.amount) }
//	}

	//Equipment
	fun getEquipment(optionKey: String): List<EquipmentRequirement> {
		val option = getOption<Map<Int, Int>>(optionKey)
		return option.map {
			EquipmentRequirement(
				it.key,
				Slot.forIndex(it.value)!!,
			)
		}
	}

	val meleeEquipment by lazy { getEquipment(MELEE_EQUIPMENT_OPTION) }
	val rangeEquipment by lazy { getEquipment(RANGE_EQUIPMENT_OPTION) }
	val mageEquipment by lazy { getEquipment(MAGE_EQUIPMENT_OPTION) }

	val allEquipmentItems by lazy { (meleeEquipment.map { it.item } + rangeEquipment.map { it.item } + mageEquipment.map { it.item }).distinct() }
	val ammos: List<Item> by lazy {
		val mutableAmmoList = mutableListOf<IEquipmentItem>()
		val meleeAmmo = meleeEquipment.firstOrNull { it.slot == Slot.QUIVER }
		val rangeAmmo = rangeEquipment.firstOrNull { it.slot == Slot.QUIVER }
		val mageAmmo = mageEquipment.firstOrNull { it.slot == Slot.QUIVER }
		if (meleeAmmo != null)
			mutableAmmoList.add(meleeAmmo.item)
		if (rangeAmmo != null)
			mutableAmmoList.add(rangeAmmo.item)
		if (mageAmmo != null)
			mutableAmmoList.add(mageAmmo.item)
		mutableAmmoList.distinct()
	}
	val ammoIds by lazy { ammos.map { it.id }.toIntArray() }

	val teleportEquipments by lazy {
		allEquipmentItems.mapNotNull { TeleportEquipment.getTeleportItem(it.id) }
	}
	var equipment: List<EquipmentRequirement> = emptyList()

	//Loot
	fun isLootWatcherActive() = lootWachter?.active == true
	var lootWachter: LootWatcher? = null
	var kills = 0
	val lootList = mutableListOf<GroundItem>()


	fun watchLootDrop(tile: Tile) {
		if (!isLootWatcherActive()) {
			logger.info("Waiting for loot at $tile")
			lootWachter = LootWatcher(tile, ammoIds, lootList = lootList, isLoot = { it.isLoot() })
		} else {
			logger.info("Already watching loot at tile: $tile for loot")
		}
	}

	fun GroundItem.isLoot() = isLoot(stackSize())

	private fun GenericItem.isLoot(amount: Int): Boolean {
		val nameLC = name().lowercase()
		return lootNames.any { ln -> nameLC.contains(ln) } || getPrice() * amount >= if (stackable()) 500 else 2000
	}

	var npcDeathWatchers: MutableList<NpcDeathWatcher> = mutableListOf()

	//Banking option
	var forcedBanking = false
	var lastTrip = false
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val npcTeleport = TeleportMethod(ItemTeleport.ROYAL_SEED_POD)
	var currentTarget: Npc = Npc.Nil
	val aggressionTimer = Timer(15 * 60 * 1000)

	fun nearbyMonsters(): List<Npc> =
		Npcs.stream().within(centerTile, 20).name(DEMONIC_GORILLA).nearest().list()

	private fun Npc.attackingOtherPlayer(): Boolean {
		val interacting = interacting()
		return interacting is Player && interacting != Players.local()
	}

//	private val GWD_AREA = Area(Tile(2816, 5120), Tile(3008, 5376))

	fun target(): Npc {
		val local = Players.local()
		val nearbyMonsters =
			nearbyMonsters().filterNot { it.healthBarVisible() && (it.attackingOtherPlayer() || it.healthPercent() == 0) }
				.sortedBy { it.distance() }
		val attackingMe =
			nearbyMonsters.firstOrNull { it.interacting() is Npc || it.interacting() == local && it.reachable() }
		return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() } ?: Npc.Nil
	}

	//Safespot options

	val centerTile = Tile(2104, 5653, 0)
	val buryBones by lazy { getOption<Boolean>(BURY_BONES_OPTION) }

	//Prayer options
	var protectionPrayer = Prayer.Effect.PROTECT_FROM_MISSILES
	val meleeOffensivePrayer by lazy { Prayer.Effect.values().firstOrNull { it.name == getOption<String>(MELEE_PRAYER_OPTION) } }
	val rangeOffensivePrayer by lazy { Prayer.Effect.values().firstOrNull { it.name == getOption<String>(RANGE_PRAYER_OPTION) } }
	val mageOffensivePrayer by lazy { Prayer.Effect.values().firstOrNull { it.name == getOption<String>(MAGE_PRAYER_OPTION) } }
	var offensivePrayer: Prayer.Effect? = null


	//Custom slayer options
	var lastTask = false
	var superiorAppeared = false
	private val slayerBraceletNames = arrayOf("Bracelet of slaughter", "Expeditious bracelet")
	fun getSlayerBracelet() = Inventory.stream().name(*slayerBraceletNames).first()
	fun wearingSlayerBracelet() = Equipment.stream().name(*slayerBraceletNames).isNotEmpty()
	val hasSlayerBracelet by lazy { getSlayerBracelet().valid() }

	var demonicPrayer: DemonicPrayer = DemonicPrayer.NONE
	fun switchStyle(prayer: DemonicPrayer) {
		if (demonicPrayer != prayer) {
			logger.info("Switching to prayer $demonicPrayer")
		}
		demonicPrayer = prayer
		when (prayer) {
			DemonicPrayer.RANGE -> {
				if (meleeEquipment.isNotEmpty()) {
					equipment = meleeEquipment
					offensivePrayer = meleeOffensivePrayer
				} else {
					equipment = mageEquipment
					offensivePrayer = mageOffensivePrayer
				}
			}

			DemonicPrayer.MELEE -> {
				if (rangeEquipment.isNotEmpty()) {
					equipment = rangeEquipment
					offensivePrayer = rangeOffensivePrayer
				} else {
					equipment = meleeEquipment
					offensivePrayer = meleeOffensivePrayer
				}
			}

			DemonicPrayer.MAGE, DemonicPrayer.NONE -> {
				if (meleeEquipment.isNotEmpty()) {
					equipment = meleeEquipment
					offensivePrayer = meleeOffensivePrayer
				} else {
					equipment = rangeEquipment
					offensivePrayer = rangeOffensivePrayer
				}
			}
		}
	}

	@Subscribe
	fun onTickEvent(_e: TickEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val time = System.currentTimeMillis()
		projectiles.forEach {
			if (time - it.second > projectileDuration) {
				projectiles.remove(it)
			}
		}
		val interacting = me.interacting()
		if (interacting is Npc && interacting != Npc.Nil) {
			currentTarget = interacting

			val activeLW = lootWachter
			if (activeLW?.active == true && activeLW.tile == currentTarget.tile()) return
			val deathWatcher = npcDeathWatchers.firstOrNull { it.npc == interacting }
			if (deathWatcher == null || !deathWatcher.active) {
				val newDW = NpcDeathWatcher(
					interacting,
					false
				) {
					kills++
					if (hasSlayerBracelet && !wearingSlayerBracelet()) {
						val slayBracelet = getSlayerBracelet()
						if (slayBracelet.valid()) {
							getSlayerBracelet().fclick()
							logger.info("Wearing bracelet on death at ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
						}
					}
					watchLootDrop(interacting.tile())
				}
				npcDeathWatchers.add(newDW)
			}
		}
		if (currentTarget.valid() && currentTarget.name == DEMONIC_GORILLA) {
			val prayId = currentTarget.prayerHeadIconId()
			switchStyle(DemonicPrayer.forOverheadId(prayId))
		}
		npcDeathWatchers.removeAll { !it.active }
	}

	@Subscribe
	fun onInventoryChange(evt: InventoryChangeEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val id = evt.itemId
		val isTeleport = TeleportEquipment.isTeleportItem(id)
		if (evt.quantityChange > 0 && id != VIAL
			&& requiredInventory.none { id in it.item.ids }
			&& allEquipmentItems.none { it.ids.contains(id) }
			&& !isTeleport
		) {
			painter.trackItem(id, evt.quantityChange)
		}
	}

	@Subscribe
	fun onAnimationChangeEvent(e: NpcAnimationChangedEvent) {
		if (e.npc.name != DEMONIC_GORILLA) return
		val anim = e.animation
		if (anim == DEMONIC_GORILLA_DEATH_ANIM) {
			lootWachter = LootWatcher(e.npc.tile(), ammoIds, isLoot = { it.isLoot() }, lootList = lootList)
		}
		protectionPrayer = when (e.animation) {
			DEMONIC_GORILLA_MELEE_ANIM -> Prayer.Effect.PROTECT_FROM_MELEE
			DEMONIC_GORILLA_RANGE_ANIM -> Prayer.Effect.PROTECT_FROM_MISSILES
			DEMONIC_GORILLA_MAGE_ANIM -> Prayer.Effect.PROTECT_FROM_MAGIC
			else -> Prayer.Effect.PROTECT_FROM_MISSILES
		}
	}

	@Subscribe
	fun experienceEvent(xpEvent: SkillExpGainedEvent) {
		if (xpEvent.skill == Skill.Slayer) {
			logger.info("Slayer xp at: ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
		} else if (xpEvent.skill == Skill.Hitpoints) {
			val dmg = floor(xpEvent.expGained / 1.33).toInt()
			if (TargetWidget.health() - dmg <= 0 && hasSlayerBracelet && !wearingSlayerBracelet()) {
				val slayBracelet = getSlayerBracelet()
				if (slayBracelet.valid()) {
					logger.info("Wearing bracelet on xp drop ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
					slayBracelet.fclick()
				}
			}
		}
	}

	var projectiles = mutableListOf<Pair<Projectile, Long>>()
	var projectileSafespot = Tile.Nil
	val projectileDuration = 2400
	var fightingFromDistance = false

	private fun findSafeSpotFromProjectile() {
		val dangerousTiles = projectiles.map { it.first.destination() }
		val myTile = me.tile()
		val collisionMap = Movement.collisionMap(myTile.floor).collisionMap.flags
		val grid = mutableListOf<Pair<Tile, Double>>()
		for (x in -2 until 2) {
			for (y in -2 until 2) {
				val t = Tile(myTile.x + x, myTile.y + y, myTile.floor)
				if (!t.blocked(collisionMap)) {
					grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
				}
			}
		}
		projectileSafespot = grid.maxByOrNull { it.second }!!.first
	}

	@Subscribe
	fun onProjectile(e: ProjectileDestinationChangedEvent) {
		if (e.target() == Actor.Nil) {
			val distance = e.destination().distance()
			if (distance < 2) {
				logger.info("Dangerous projectile spawned! tile=${e.destination()}")
				projectiles.add(e.projectile to System.currentTimeMillis())
				findSafeSpotFromProjectile()
				if (distance == 0.0) {
					Movement.step(projectileSafespot, 0)
				}
			}
		} else if (e.target() == currentTarget) {
			fightingFromDistance = true
		}
	}

	@Subscribe
	fun messageReceived(msg: MessageEvent) {
		if (msg.messageType != MessageType.Game) return
		if (msg.message.contains("so you can't take ")) {
			logger.info("Ironman message CANT TAKE type=${msg.messageType}")
			lootList.clear()
		}
		if (msg.message.contains("A superior foe has appeared")) {
			logger.info("Superior appeared message received: type=${msg.messageType}")
			superiorAppeared = true
		}
	}

	@Subscribe
	fun onPaintCheckbox(pcce: PaintCheckboxChangedEvent) {
		if (pcce.checkboxId == "stopAfterTask") {
			lastTask = pcce.checked
			val painter = painter as DGPainter
			if (pcce.checked && !painter.paintBuilder.items.contains(painter.slayerTracker)) {
				val index =
					painter.paintBuilder.items.indexOfFirst { row -> row.any { it is CheckboxPaintItem && it.id == "stopAfterTask" } }
				painter.paintBuilder.items.add(index, painter.slayerTracker)
			} else if (!pcce.checked && painter.paintBuilder.items.contains(painter.slayerTracker)) {
				painter.paintBuilder.items.remove(painter.slayerTracker)
			}
		} else if (pcce.checkboxId == "stopAtBank") {
			lastTrip = pcce.checked
		}
	}
}


fun main() {
	DemonicGorilla().startScript("127.0.0.1", "GIM", false)
}