package org.powbot.krulvis.tempoross

import com.google.common.eventbus.Subscribe
import org.powbot.api.InteractableEntity
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.rt4.walking.local.nodes.LocalEdge
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.extensions.items.Item.Companion.HAMMER
import org.powbot.krulvis.api.extensions.items.Item.Companion.IMCANDO_HAMMER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.HARPOON
import org.powbot.krulvis.tempoross.tree.branch.ShouldEnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.EnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.Leave

@ScriptManifest(
	name = "krul Tempoross",
	description = "Does tempoross minigame",
	version = "1.3.6",
	author = "Krulvis",
	scriptId = "54b4c295-8cb8-4c22-9799-49b7344708e7",
	markdownFileName = "Tempoross.md",
	category = ScriptCategory.Fishing
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = UI.LOOTING,
			description = "Loot the reward pool",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = UI.EQUIPMENT,
			description = "What gear and harpoon to equip",
			defaultValue = """{"25592":0,"21028":3,"25594":4,"25596":7,"25598":10}""",
			optionType = OptionType.EQUIPMENT
		),
		ScriptConfiguration(
			name = "info",
			description = "Inventory setup for Tempoross: " +
				"\n- Amount of buckets" +
				"\n- Harpoon (if not equipped and not barb. fishing)" +
				"\n- (Imcando) hammer",
			optionType = OptionType.INFO
		),
		ScriptConfiguration(
			name = UI.INVENTORY,
			description = "What items to take in inventory",
			defaultValue = """{"$BUCKET_OF_WATER": 5, "$HAMMER": 1}""",
			optionType = OptionType.INVENTORY
		),
		ScriptConfiguration(
			name = UI.SOLO_METHOD,
			description = "Solo method with 19 cooked fish",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = UI.COOK_FISH,
			description = "Cooking the fish gives more points (reward) at the cost of XP",
			defaultValue = "true",
			optionType = OptionType.BOOLEAN
		),
		ScriptConfiguration(
			name = UI.SPECIAL_ATTACK,
			description = "Do Dragon/Inferno Harpoon special",
			defaultValue = "false",
			optionType = OptionType.BOOLEAN
		),
	]
)
class Tempoross : ATScript() {
	override val rootComponent: TreeComponent<*> = ShouldEnterBoat(this)

	@ValueChanged(UI.SOLO_METHOD)
	fun onValueChanged(solo: Boolean) {
		if (solo) {
			updateOption(UI.COOK_FISH, true, OptionType.BOOLEAN)
		}
		updateVisibility(UI.COOK_FISH, !solo)
	}

	override fun createPainter(): ATPaint<*> {
		return TemporossPaint(this)
	}

	var gameTick = -1
	var waveTick = -1
	var side = Side.UNKNOWN
	val burningTiles = mutableListOf<Tile>()
	val triedPaths = mutableListOf<LocalPath>()
	var rewardGained = 0
	var pointsObtained = 0
	var rounds = 0
	var lastGame = false
	var debugPaint = false
	var vulnerableStartHP = 100
	var bestFishSpot: Npc? = null
	var fishSpots: List<Pair<Npc, LocalPath>> = emptyList()
	val hasSpiritOutfit by lazy { intArrayOf(25592, 25594, 25596, 25598).all { it in equipment.keys } }

	val cookFish by lazy { getOption<Boolean>(UI.COOK_FISH) }
	val spec by lazy { getOption<Boolean>(UI.SPECIAL_ATTACK) }
	val equipment by lazy { getOption<Map<Int, Int>>(UI.EQUIPMENT) }
	val inventory by lazy { getOption<Map<Int, Int>>(UI.INVENTORY) }
	val solo by lazy { getOption<Boolean>(UI.SOLO_METHOD) }
	val buckets by lazy {
		if (solo) 5 else inventory.filter {
			it.key in intArrayOf(
				BUCKET_OF_WATER,
				EMPTY_BUCKET
			)
		}.values.sum()
	}
	val inventoryBankItems by lazy {
		inventory.filterNot {
			it.key in intArrayOf(
				BUCKET_OF_WATER,
				EMPTY_BUCKET,
				ROPE,
				HARPOON,
				HAMMER
			)
		}
	}

	var intensity = -1
		private set
	var energy = -1
		private set
	var health = -1
		private set

	val energyGoingDownTimer = Timer(2000)

	@Subscribe
	fun onGameTick(_e: TickEvent) {
		gameTick++
		intensity = Data.getIntensity()
		val newEnergy = Data.getEnergy()
		if (newEnergy < energy) {
			energyGoingDownTimer.reset()
		}
		energy = Data.getEnergy()
		health = Data.getHealth()
		detectDangerousTiles()
		collectFishSpots()
		bestFishSpot = getClosestFishSpot(fishSpots)
	}

	fun cookedToSubdue() = Math.round(energy / 5.625)

	fun getNearestFire() = Npcs.stream().within(12).within(side.area).name("Fire").nearest().firstOrNull()
	fun getRelevantInventoryItems(): Map<Int, Int> =
		Inventory.get { it.id in inventory.keys }
			.groupBy { it.id }
			.mapValues { it.value.sumOf { i -> i.stack } }

	fun hasDangerousPath(end: Tile): Boolean {
		val path = LocalPathFinder.findPath(end)
		return containsDangerousTile(path)
	}

	private fun containsDangerousTile(path: LocalPath): Boolean {
		triedPaths.add(path)
		return path.actions.any { burningTiles.contains(it.destination) }
	}

	fun interactWhileDousing(
		e: InteractableEntity?,
		action: String,
		destinationWhenNil: Tile,
		allowCrossing: Boolean
	): Boolean {
		if (e == null) {
			logger.info("Can't find: $action")
			if (destinationWhenNil != Tile.Nil) {
				val path = LocalPathFinder.findPath(destinationWhenNil)
				if (path.isNotEmpty() && douseIfNecessary(path, allowCrossing)) {
					if (walkPath(path)) {
						getTrash()?.interact("Drop")
					}
				} else {
					logger.info(if (path.isEmpty()) "Path is empty" else "failed dousing")
					walk(destinationWhenNil)
				}
			}
		} else {
			var path = LocalPathFinder.findPath(e.tile())
			if (path.isEmpty()) path = LocalPathFinder.findPath(destinationWhenNil)
			if (douseIfNecessary(path, allowCrossing)) {
				return walkAndInteract(e, action)
			}
		}
		return false
	}

	fun walkWhileDousing(tile: Tile, allowCrossing: Boolean): Boolean {
		return walkWhileDousing(LocalPathFinder.findPath(tile), allowCrossing)
	}

	fun walkWhileDousing(path: LocalPath, allowCrossing: Boolean): Boolean {
		if (douseIfNecessary(path, allowCrossing)) {
			if (!walkPath(path)) {
				return false
			}
			getTrash()?.interact("Drop")
		}
		return true
	}

	private fun getTrash(): Item? {
		val inventory = Inventory.stream().toList().groupBy { it.id }.mapValues { ig -> ig.value.sumOf { it.stack } }
		val totalBuckets = inventory.getOrDefault(EMPTY_BUCKET, 0) + inventory.getOrDefault(BUCKET_OF_WATER, 0)
		if (inventory.getOrDefault(HAMMER, 0) > 1) {
			return Inventory.stream().id(HAMMER).firstOrNull()
		} else if (totalBuckets > buckets)
			return Inventory.stream().id(EMPTY_BUCKET, BUCKET_OF_WATER).firstOrNull()
		return null
	}

	private fun douseIfNecessary(path: LocalPath, allowCrossing: Boolean = false): Boolean {
		val blockedTile = path.actions.firstOrNull { burningTiles.contains(it.destination) }
		val fire = blockedTile?.getFire()
		val hasBucket = Inventory.containsOneOf(BUCKET_OF_WATER)
		logger.info("Blockedtile: $blockedTile fire: $fire, Bucket: $hasBucket")
		if (fire != null && hasBucket) {
			logger.info("Found fire on the way to: ${path.finalDestination()}")
			if (!fire.inViewport()) {
				val blockedIndex = path.actions.indexOf(blockedTile)
				if (fire.distance() > 8 && blockedIndex >= 2) {
					val safeSpot = path.actions[blockedIndex - 2].destination
					Movement.step(safeSpot)
				}
				Camera.turnTo(fire)
				waitFor(long()) { fire.inViewport() }
			} else if (fire.interact("Douse")) {
				return waitFor(long()) { Npcs.stream().at(fire).name("Fire").isEmpty() }
			}
		} else if (fire == null || allowCrossing) {
			logger.info(if (fire == null) "No fire on the way" else "allowCrossing=true")
			return true
		}
		return false
	}

	private fun LocalEdge.getFire(): Npc? = Npcs.stream().name("Fire").nearest(destination).firstOrNull()

	private fun walkPath(path: LocalPath): Boolean {
		if (path.isEmpty()) {
			return false
		}

		val finalTile = path.actions.last().destination
		return if (finalTile.matrix().onMap() && finalTile.distance() > 5) {
			Movement.step(finalTile)
		} else if (path.isNotEmpty()) {
			path.traverse()
		} else {
			false
		}
	}

	fun isVulnerable(): Boolean = energy in 0..2 || getBossPool().valid()

	fun isLowHP(): Boolean = health <= if (solo) 33 else 5

	fun isTethering(): Boolean = (Varpbits.varpbit(2933) and 1) == 1

	fun isWaveActive() = gameTick - waveTick <= 15

	override fun canBreak(): Boolean {
		val canBreak = lastLeaf is EnterBoat || lastLeaf is Leave
		logger.info("canBreak() lastLeaf=${lastLeaf.name} canBreak=${canBreak}")

		return canBreak
	}

	@Subscribe
	fun onNpcAnimationEvent(e: NpcAnimationChangedEvent) {
		logger.info("AnimationChangeEvent for npc=${e.npc.name}, animation=${e.animation}")
	}

	@Subscribe
	fun onBreakEvent(be: BreakEvent) {
		if (!canBreak()) {
			be.delay(1000)
		}
	}

	@Subscribe
	fun onMessageEvent(me: MessageEvent?) {
		if (me == null || me.type != 0) {
			return
		}
		val txt = me.message
		logger.info(txt)
		if (txt.contains("Points: ") && txt.contains("Personal best", true)) {
			val points = txt.substring(20, txt.indexOf("</col>")).replace(",", "").toInt()
			logger.info("Finished round, gained: $points points")
			pointsObtained += points
			rounds++
		} else if (txt.contains("Tempoross is vulnerable!")) {
			vulnerableStartHP = health
		} else if (txt.contains("A colossal wave closes in...")) {
			logger.info("Should tether wave coming in!")
			waveTick = gameTick
		} else if (isWaveOverText(txt)) {
			logger.info("Wave is over after ${gameTick - waveTick} ticks")
		} else if (txt.contains("Reward permits: ") && txt.contains("Total permits:")) {
			val reward = txt.substring(28, txt.indexOf("</col>")).toInt()
			logger.info("Gained $reward points")
			rewardGained += reward
		}
	}

	private fun isWaveOverText(msg: String): Boolean {
		return msg.contains("...the rope keeps you securely upright as the wave washes over you")
			|| msg.contains("...the wave slams into you, knocking you to the ground.")
	}

	@com.google.common.eventbus.Subscribe
	fun onCheckBoxEvent(e: PaintCheckboxChangedEvent) {
		if (e.checkboxId == "lastGame") {
			lastGame = e.checked
		} else if (e.checkboxId == "paintDebug") {
			debugPaint = e.checked
		}
	}


	private fun Npc.rightSide(): Boolean {
		val y = tile().y
		return if (side == Side.NORTH) {
			y > side.mastLocation.y()
		} else {
			y < side.mastLocation.y()
		}
	}

	/**
	 * Detect and add blocked tiles.
	 */
	fun detectDangerousTiles() {
		burningTiles.clear()
		Npcs.stream().name("Lightning cloud", "Fire").within(side.area).forEach {
			if (it.name != "Lightning cloud" || it.animation() > -1) {
				logger.info("Found Lightning/Fire cloud at ${it.tile()}, animation=${it.animation()}")
				addTile(it.tile())
			}
		}
	}

	private fun addTile(tile: Tile) {
		burningTiles.add(tile)
		burningTiles.add(Tile(tile.x(), tile.y() - 1, 0))
		burningTiles.add(Tile(tile.x() - 1, tile.y(), 0))
		burningTiles.add(Tile(tile.x() - 1, tile.y() - 1, 0))
	}

	fun collectFishSpots() {
		fishSpots = Npcs.stream().filtered { it.rightSide() }
			.action("Harpoon").name("Fishing spot").list()
			.map { Pair(it, LocalPathFinder.findPath(it.tile().getWalkableNeighbor())) }
	}

	fun getClosestFishSpot(spots: List<Pair<Npc, LocalPath>>): Npc? {
		val doublePath = spots.firstOrNull { it.first.id() == DOUBLE_FISH_ID }
		if (doublePath != null) {
			return doublePath.first
		}

		return spots.filter { !containsDangerousTile(it.second) }.minByOrNull { it.second.actions.size }?.first
	}

	fun getBossPool() =
		Npcs.stream().at(side.bossPoolLocation).action("Harpoon").name("Spirit pool").first()

	private val stunnedAnimation = 832
	fun getAmmoCrate(): Npc {
		val npcs = Npcs.stream().within(side.area).toList()
		val crates = npcs.filter { it.name.contains("Ammunition crate", true) }
		logger.info("Found Npcs=${npcs.count()}, found crates=${crates.count()}")
		return crates.filter { crate ->
			npcs.none { it.name == "Cannoneer" && it.x() == crate.x() && it.animation() == stunnedAnimation }
		}.minByOrNull { it.distance() } ?: Npc.Nil
	}

	fun nearAmmoCrate(): Boolean {
		return Npcs.stream().name("Ammunition crate").within(3).isNotEmpty()
	}

	fun hasHammer() = Inventory.containsOneOf(HAMMER, IMCANDO_HAMMER)
	fun getHammerContainer(): GameObject = Objects.stream()
		.type(GameObject.Type.INTERACTIVE).within(side.area).name("Hammers").first()

	fun getRopeContainer(): GameObject = Objects.stream(50)
		.type(GameObject.Type.INTERACTIVE).within(side.area)
		.name("Ropes").first()

	fun getBucketCrate(): GameObject =
		Objects.stream(50).type(GameObject.Type.INTERACTIVE).within(side.area).name("Buckets").nearest().first()

	fun getWaterpump(): GameObject {
		return if (side == Side.UNKNOWN) {
			Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Water pump").nearest().first()
		} else
			Objects.stream(50).type(GameObject.Type.INTERACTIVE).within(side.area).name("Water pump").nearest().first()
	}

	fun getTetherPole(): GameObject {
		val dest = Movement.destination()
		val destination = if (dest != Tile.Nil) dest else me.tile()
		val validTiles = listOf(side.totemLocation, side.mastLocation)
		return Objects.stream(50).type(GameObject.Type.INTERACTIVE).within(side.area).filtered {
			validTiles.contains(it.tile())
		}.action("Repair", "Tether").nearest(destination).first()
	}

	fun getLadder(): GameObject = Objects.stream(50).name("Rope ladder").action("Climb").first()

	fun getEmptyBuckets(): Int = Inventory.stream().id(EMPTY_BUCKET).count().toInt()
	fun getFilledBuckets(): Int = Inventory.stream().id(BUCKET_OF_WATER).count().toInt()
	fun getTotalBuckets(): Int = Inventory.stream().id(EMPTY_BUCKET, BUCKET_OF_WATER).count().toInt()

	fun playersReady(): Int {
		if (!Widgets.widget(687).valid()) {
			return 0
		}
		val text = Widgets.component(687, 3).text()
		val digits = text.substring(text.indexOf("<col=ffffff>") + 12, text.indexOf("</col>"))
		return if (digits == "None") 0 else digits.toInt()
	}
}

fun main() {
	Tempoross().startScript("127.0.0.1", "GIM", false)
}
