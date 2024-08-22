package org.powbot.krulvis.tithe

import com.google.common.eventbus.Subscribe
import org.powbot.api.*
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.tithe.Data.NAMES
import org.powbot.krulvis.tithe.Patch.Companion.isPatch
import org.powbot.krulvis.tithe.tree.branch.ShouldStart
import org.powbot.krulvis.tithe.tree.leaf.Refill
import org.powbot.krulvis.tithe.tree.leaf.Start
import org.powbot.mobile.script.ScriptManager
import kotlin.math.min

@ScriptManifest(
	name = "krul Tithe",
	description = "Tithe farming mini-game",
	author = "Krulvis",
	version = "1.1.4",
	scriptId = "97078671-3780-4a44-b488-36ef241686dd",
	markdownFileName = "Tithe.md",
	category = ScriptCategory.Farming,
	singleTapRequired = false
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Smart Patches",
			description = "17, 17, 17, 17, 16, 16",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		),
		ScriptConfiguration(
			name = "Patches",
			description = "How many patches do you want to use? Max 20",
			optionType = OptionType.INTEGER,
			defaultValue = "16"
		),
		ScriptConfiguration(
			name = "Crash Watcher",
			description = "Stop script if being crashed?",
			optionType = OptionType.BOOLEAN,
			defaultValue = "true"
		)
	]
)
class TitheFarmer : ATScript() {

	override val rootComponent: TreeComponent<*> = ShouldStart(this)

	val _patchCount by lazy { min(getOption<Int>("Patches").toInt(), 20) }
	val smartPatches by lazy { getOption<Boolean>("Smart Patches") }
	val crashWatcher by lazy { getOption<Boolean>("Crash Watcher") }
	var currentTick = -1L
	var lastPatch: Patch? = null
	var nextPatch: Patch? = null
	var lastRound = false
	var startPoints = -1
	var gainedPoints = 0
	var patches = listOf<Patch>()
	val chillTimer = Timer(5000)
	var planting = false

	val patchCount
		get() = if (_patchCount == 16 && smartPatches && seedCount() >= 32) 17 else _patchCount

	override fun onStart() {
		super.onStart()
	}

	override fun createPainter(): ATPaint<*> {
		return TithePainter(this)
	}

	fun getCornerPatchTile(): Tile {
		val allPatches = Objects.stream(30).filtered { it.isPatch() }.list()
		val maxX = allPatches.minOf { it.tile().x() }
		val maxY = allPatches.maxOf { it.tile().y() }
		return Tile(maxX, maxY, 0)
	}

	fun getPatchTiles(): List<Tile> {
		val tiles = mutableListOf(getCornerPatchTile())
		val columns = mutableListOf<Tile>()
		tiles.add(Tile(tiles[0].x(), tiles[0].y() - 15))
		tiles.forEach {
			for (y in 0..9 step 3) {
				columns.add(Tile(it.x(), it.y() - y))
				columns.add(Tile(it.x() + 5, it.y() - y))
			}
		}

		val lastPatch = Tile(tiles[0].x() + 10, tiles[0].y() - 9)
		for (y in 0..9 step 3) {
			columns.add(Tile(lastPatch.x(), lastPatch.y() + y))
		}

		//Do 17 for the first couple rounds to skip the round where you do just 4 plants
		return columns.toList().subList(0, patchCount)
	}

	fun refreshPatches() {
		val tiles = getPatchTiles()
		val onorderedPatches =
			Objects.stream(35)
				.filtered { it.name().isNotEmpty() && it.name() != "null" && it.tile() in tiles }
				.list()
		patches = tiles.mapIndexed { index, tile ->
			Patch(onorderedPatches.firstOrNull { it.tile() == tile } ?: GameObject.Nil, tile, index)
		}

	}

	fun hasSeeds(): Boolean = getSeed() > 0

	override fun canBreak(): Boolean {
		return lastLeaf is Start || lastLeaf is Refill
	}

	fun getSeed(): Int {
		val seed = Inventory.stream().id(*Data.SEEDS).findFirst()
		return if (seed.isPresent) seed.get().id() else -1
	}

	fun seedCount(): Int {
		return Inventory.stream().id(*Data.SEEDS).count(true).toInt()
	}

	fun getPoints(): Int {
		val widget =
			Widgets.widget(241).components().firstOrNull { it.text().contains("Points:") } ?: return -1
		return widget.text().substring(8).toInt()
	}

	fun getWaterCount(): Int = Inventory.stream().id(*Data.WATER_CANS).list().sumOf { it.id() - 5332 }

	fun hasEnoughWater(): Boolean = getWaterCount() >= patchCount * 3

	@Subscribe
	fun onGameActionEvent(evt: GameActionEvent) {
		if (evt.rawOpcode == 4 || evt.opcode() == GameActionOpcode.InteractObject) {
			val tile = Tile(evt.var0 + 1, evt.widgetId + 1, 0).globalTile()
			if (NAMES.any { evt.rawEntityName.contains(it, true) }) {
				lastPatch = patches.first { it.tile == tile }
				logger.info("Interacted ${evt.interaction} on $lastPatch")
			}
		}
	}

	@ValueChanged("Smart Patches")
	fun onValueChange(smart: Boolean) {
		updateEnabled("Patches", !smart)
		if (smart) {
			updateOption("Patches", 16, OptionType.INTEGER)
		}
	}

	@com.google.common.eventbus.Subscribe
	fun onCheckBoxEvent(e: PaintCheckboxChangedEvent) {
		if (e.checkboxId == "lastRound") {
			lastRound = e.checked
		}
	}

	@com.google.common.eventbus.Subscribe
	fun onMsg(e: MessageEvent) {
		logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
		if (e.message.contains("This plant was planted by ") && crashWatcher) {
			Notifications.showNotification("World is crashed... stopping script")
			ScriptManager.stop()
		}
	}

	@Subscribe
	fun onGameTick(e: TickEvent) {
		currentTick = System.currentTimeMillis()
	}


	fun interact(
		target: Interactive?,
		action: String,
		selectItem: Int = -1,
		useMenu: Boolean = true
	): Boolean {
		val t = target ?: return false
		val name = (t as Nameable).name()
		val pos = (t as Locatable).tile()
		val destination = Movement.destination()
		turnRunOn()
		if (!t.inViewport()
			|| (destination != pos && pos.distanceTo(if (destination == Tile.Nil) Players.local() else destination) > 12)
		) {
			logger.info("Walking before interacting... in viewport: ${t.inViewport()}")
			if (pos.matrix().onMap()) {
				Movement.step(pos)
			} else {
				LocalPathFinder.findWalkablePath(pos).traverse()
			}
		}
		val selectedId = Inventory.selectedItem().id()
		if (selectedId != selectItem) {
			Game.tab(Game.Tab.INVENTORY)
			if (selectItem > -1) {
				Inventory.stream().id(selectItem).firstOrNull()?.interact("Use")
			} else {
				Inventory.stream().id(selectedId).firstOrNull()?.click()
			}

		}
		val interactBool =
			if (name == null || name == "null" || name.isEmpty()) t.interact(action, useMenu) else t.interact(
				action,
				name,
				useMenu
			)
		return Condition.wait {
			Inventory.selectedItemIndex() == -1 || Inventory.selectedItem().id() == selectItem
		} && interactBool
	}

	fun turnRunOn(): Boolean {
		if (Movement.running()) {
			return true
		}
		if (Movement.energyLevel() >= Random.nextInt(1, 5)) {
			return Widgets.widget(Constants.MOVEMENT_MAP).component(Constants.MOVEMENT_RUN_ENERGY - 1).click()
		}
		return false
	}


}

fun main() {
	TitheFarmer().startScript(false)
}