package org.powbot.krulvis.lizardshamans

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.lizardshamans.Data.LOOT
import org.powbot.krulvis.lizardshamans.event.JumpEvent
import org.powbot.krulvis.lizardshamans.tree.branch.ShouldBank

@ScriptManifest("krul LizardmanShamans", "Kills lizardman shamans for Dragon Warhammer", version = "1.0.0", priv = true)
@ScriptConfiguration.List([
	ScriptConfiguration("Equipment", "What to wear?", optionType = OptionType.EQUIPMENT),
	ScriptConfiguration("Inventory", "What to take with?", optionType = OptionType.INVENTORY),
	ScriptConfiguration("Slayer", "Kill on task?", optionType = OptionType.BOOLEAN)
])
class LizardShamans : ATScript() {
	override fun createPainter(): ATPaint<*> = LizardShamanPainter(this)

	var kills = 0
	val lootList = mutableListOf<GroundItem>()
	var target: Npc = Npc.Nil
	var lastAnimation = -1
	var jumpEvent = JumpEvent(Tile.Nil)
	val escapeTiles = mutableListOf<Pair<Tile, Double>>()
	var escapeTile = Tile.Nil
	val spawns = mutableListOf<Npc>()
	val skippedLoot = mutableListOf<GroundItem>()

	@Subscribe
	fun onTick(tickEvent: TickEvent) {
		val interacting = me.interacting()
		if (interacting.valid() && interacting is Npc) {
			target = interacting
		}

		watchForLoot()
		findSpawns()
		escapeTiles.clear()
		if (!target.valid() && spawns.isEmpty()) return

		findEscapeTile()
	}

	private fun findSpawns() {
		spawns.clear()
		spawns.addAll(Npcs.stream().name("Spawn").within(10).toList())
	}

	private fun watchForLoot() {
		GroundItems.stream().within(15)
			.filtered { !skippedLoot.contains(it) && !lootList.contains(it) && it.isLoot() }
			.forEach { gi ->
				lootList.add(gi)
			}
	}

	private fun findEscapeTile() {
		val myTile = me.tile()
		val collisionMap = Movement.collisionMap(myTile.floor).flags()
		val tiles = Data.furthestReachableTiles(myTile, collisionMap)
		val nearest = tiles.minByOrNull { it.distance() }
		escapeTiles.addAll(tiles.filterNot { it == nearest }.map {
			val (a, b, c) = Data.lineEquation(myTile, it)
			it to Data.averagePerpendicularDistance(spawns + target, a, b, c)
		})
		escapeTile = escapeTiles.maxByOrNull { it.second }?.first ?: Tile.Nil
	}

	@Subscribe
	fun onNpcAnimation(nae: NpcAnimationChangedEvent) {
		val npc = nae.npc
		val anim = nae.animation
		if (npc.name == "Lizardman shaman") {
			val attack = Data.AttackAnimation.forAnimation(anim)
			logger.info("Shaman Animation=$anim, attack=${attack}")
			if (npc == target) {
				lastAnimation = anim
				if (attack == Data.AttackAnimation.Jump) {
					jumpEvent = JumpEvent(me.tile())
				} else if (attack == Data.AttackAnimation.Falling) {
					jumpEvent.timer.stop()
				}
			}
			if (npc.dead()) {
				kills++
//				LootWatcher(npc.tile(), -1, lootList = lootList) { it.isLoot() }
			}
		}
	}

	@Subscribe
	fun onInventoryChange(i: InventoryChangeEvent) {
		val name = i.itemName
		if (name in LOOT) {
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


	fun GroundItem.isLoot(): Boolean {
		val name = name()
		return name in LOOT
			|| (name == "Coins" && stackSize() > 1000)
			|| (Inventory.emptySlotCount() > 2 && name == "Chilli potato")
	}

	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}

fun main() {
	LizardShamans().startScript()
}