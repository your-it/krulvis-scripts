package org.powbot.krulvis.dagannothkings

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.requirements.EquipmentRequirement
import org.powbot.krulvis.dagannothkings.Data.EQUIPMENT_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.KILL_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.KING_DEATH_ANIM
import org.powbot.krulvis.dagannothkings.Data.King.Companion.king
import org.powbot.krulvis.dagannothkings.Data.OFFENSIVE_PRAY_PREFIX_OPTION
import org.powbot.krulvis.dagannothkings.Data.SAFESPOT_REX
import org.powbot.krulvis.dagannothkings.tree.branch.ShouldBank
import org.powbot.mobile.script.ScriptManager

fun main() {
	DagannothKings().startScript(useDefaultConfigs = true)
}

@ScriptManifest(
	"krul DagannothKings",
	"Kills Dagannoth Kings",
	version = "1.0.0",
	scriptId = "f6ac533c-0aee-4992-aea7-10460ed56c8c",
	category = ScriptCategory.Combat,
	priv = true
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(KILL_PREFIX_OPTION + "Rex", "Kill Rex", OptionType.BOOLEAN, defaultValue = "true"),
		ScriptConfiguration(SAFESPOT_REX, "Lure rex to safespot", OptionType.BOOLEAN, defaultValue = "true"),
		ScriptConfiguration(EQUIPMENT_PREFIX_OPTION + "Rex", "Equipment for Rex", OptionType.EQUIPMENT),
		ScriptConfiguration(
			OFFENSIVE_PRAY_PREFIX_OPTION + "Rex", "Offensive Rex Prayer", OptionType.STRING,
			allowedValues = ["NONE", "MYSTIC_WILL", "MYSTIC_MIGHT", "AUGURY"], defaultValue = "NONE"
		),


		ScriptConfiguration(KILL_PREFIX_OPTION + "Supreme", "Kill Supreme", OptionType.BOOLEAN, defaultValue = "true"),
		ScriptConfiguration(
			EQUIPMENT_PREFIX_OPTION + "Supreme",
			"Equipment for Supreme",
			OptionType.EQUIPMENT,
			visible = false
		),
		ScriptConfiguration(
			OFFENSIVE_PRAY_PREFIX_OPTION + "Supreme",
			"Offensive Supreme Prayer",
			OptionType.STRING,
			visible = false,
			allowedValues = ["NONE", "HAWK_EYE", "EAGLE_EYE", "RIGOUR"],
			defaultValue = "NONE"
		),


		ScriptConfiguration(KILL_PREFIX_OPTION + "Prime", "Kill Prime", OptionType.BOOLEAN, defaultValue = "true"),
		ScriptConfiguration(
			EQUIPMENT_PREFIX_OPTION + "Prime",
			"Equipment for Prime",
			OptionType.EQUIPMENT,
			visible = false
		),
		ScriptConfiguration(
			OFFENSIVE_PRAY_PREFIX_OPTION + "Prime",
			"Offensive Prime Prayer",
			OptionType.STRING,
			visible = false,
			allowedValues = ["NONE", "ULTIMATE_STRENGTH", "CHIVALRY", "PIETY"],
			defaultValue = "NONE"
		),
		ScriptConfiguration("BankTeleport", "Which teleport to go to bank?", OptionType.STRING)
	]
)
class DagannothKings : ATScript() {

	val lootList = mutableListOf<GroundItem>()
	val skippedLoot = mutableListOf<GroundItem>()

	@ValueChanged(KILL_PREFIX_OPTION + "Rex")
	fun onRex(rex: Boolean) {
		updateVisibility(SAFESPOT_REX, rex)
		updateVisibility(OFFENSIVE_PRAY_PREFIX_OPTION + "Rex", rex)
		updateVisibility(EQUIPMENT_PREFIX_OPTION + "Rex", rex)
	}

	@ValueChanged(KILL_PREFIX_OPTION + "Supreme")
	fun onSupreme(supreme: Boolean) {
		updateVisibility(OFFENSIVE_PRAY_PREFIX_OPTION + "Supreme", supreme)
		updateVisibility(EQUIPMENT_PREFIX_OPTION + "Supreme", supreme)
	}

	@ValueChanged(KILL_PREFIX_OPTION + "Prime")
	fun onPrime(prime: Boolean) {
		updateVisibility(OFFENSIVE_PRAY_PREFIX_OPTION + "Prime", prime)
		updateVisibility(EQUIPMENT_PREFIX_OPTION + "Prime", prime)
	}

	override fun onStart() {
		super.onStart()
		Data.King.values().forEach {
			it.offensivePrayer = Prayer.Effect.values()
				.firstOrNull { pray -> pray.name == getOption(OFFENSIVE_PRAY_PREFIX_OPTION + it.name) }
			it.equipment = getOption<Map<Int, Int>>(EQUIPMENT_PREFIX_OPTION + it.name).map { eq ->
				EquipmentRequirement(
					eq.key,
					Equipment.Slot.forIndex(eq.value)!!,
				)
			}
			it.kill = getOption(KILL_PREFIX_OPTION + it.name)
		}
	}

	var kills = 0
	var target = Npc.Nil
	val animMap: MutableMap<Data.King, Int> = mutableMapOf()
	var forcedProtectionPrayer: Prayer.Effect? = null

	//Rex settings
	var lureTile: Tile = Tile.Nil
	var safeTile: Tile = Tile.Nil
	var rexTile: Tile = Tile.Nil
	val safeSpotRex: Boolean by lazy { getOption(SAFESPOT_REX) }

	@Subscribe
	fun onGameTick(e: TickEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val me = me
		val targ = me.interacting()
		if (targ is Npc && targ.valid()) {
			target = targ
		}

		if (lureTile == Tile.Nil && Data.getKingsLadderDown().valid()) {
			logger.info("Setting lureTiles")
			val ladder = Data.getKingsLadderDown()
			lureTile = ladder.tile.derive(29, 1)
			safeTile = ladder.tile.derive(28, -8)
			rexTile = ladder.tile.derive(28, -4)
		} else if (lureTile.distance() < 50) {
			//Inside Kings lair
			setForcedProtection()
		}

		watchForLoot()
	}

	private fun setForcedProtection() {
		val offensiveKings = Npcs.stream().nameContains("Dagannoth")
			.filtered { it.king()?.kill == true && it.interacting() == me }
			.mapNotNull { it.king() }.sortedBy { it.ordinal }
			.filter { it != Data.King.Rex || !safeSpotRex }
		if (offensiveKings.isNotEmpty()) {
			val cycle = Game.cycle()
			forcedProtectionPrayer =
				offensiveKings.maxByOrNull { cycle - animMap.getOrDefault(it, -1) }?.protectionPrayer ?: return
			if (!Prayer.prayerActive(forcedProtectionPrayer!!)) {
				Prayer.prayer(forcedProtectionPrayer!!, true)
			}
		} else {
			forcedProtectionPrayer = null
		}
	}

	private fun watchForLoot() {
		GroundItems.stream().within(15)
			.filtered { !skippedLoot.contains(it) && !lootList.contains(it) && it.isLoot() }
			.forEach { gi ->
				lootList.add(gi)
			}
	}

	private fun GroundItem.isLoot(): Boolean {
		val name = name()
		return name in Data.LOOT
			|| (name == "Coins" && stackSize() > 1000)
			|| (Food.forName(name) != null)
	}


	@Subscribe
	fun onNpcAnimation(e: NpcAnimationChangedEvent) {
		val npc = e.npc
		val king = npc.king() ?: return
		val anim = e.animation
		val lastCycle = animMap.getOrDefault(king, Game.cycle())
		logger.info("NpcAnimationEvent(npc=${npc.name}, animation=${anim}, cycles=${Game.cycle() - lastCycle})")
		if (anim == king.offensiveAnim) {
			animMap[king] = Game.cycle()
			logger.info("Attack from king=$king")
		}
		if (anim == KING_DEATH_ANIM && npc == target && npc.dead()) {
			kills++
		}
	}


	@Subscribe
	fun onInventoryChange(i: InventoryChangeEvent) {
		if (ScriptManager.state() != ScriptState.Running) return
		val name = i.itemName
		if (name in Data.LOOT || name == "Coins") {
			painter.trackItem(i.itemId, i.quantityChange)
		}
	}

	@Subscribe
	fun onMessageEvent(me: MessageEvent) {
		if (me.message.contains("so you can't take ")) {
			logger.info("Ironman message CANT TAKE type=${me.messageType}")
			skippedLoot.addAll(lootList)
			lootList.clear()
		}
	}

	fun getNewTarget(): Npc {
		return Npcs.stream().nameContains("rex").filtered { !it.dead() }.nearest().first()
	}


	override fun createPainter(): ATPaint<*> = DKPaint(this)

	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}
