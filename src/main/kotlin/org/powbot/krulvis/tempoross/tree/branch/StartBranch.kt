package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.BOAT_AREA
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.*


class ShouldEnterBoat(script: Tempoross) : Branch<Tempoross>(script, "Should enter boat") {
	override fun validate(): Boolean {
		if (script.energy > -1 || BOAT_AREA.contains(me.tile())) return false
		if (script.gameTick < 0) {
			//Script hasn't had it's first gameTick yet, energy hasn't been initialized yet...
			return !waitFor { script.gameTick > 0 && script.energy > -1 }
		}
		return Game.clientState() == 30 || !waitFor(10000) { script.energy > -1 }
	}

	override val successComponent: TreeComponent<Tempoross> = HasAllEquipment(script)
	override val failedComponent: TreeComponent<Tempoross> = WaitingForStart(script)
}

class HasAllEquipment(script: Tempoross) : Branch<Tempoross>(script, "Has All Equipment") {
	override val failedComponent: TreeComponent<Tempoross> = GetEquipmentFromBank(script)
	override val successComponent: TreeComponent<Tempoross> = HasAllItemsFromBank(script)

	override fun validate(): Boolean {
		return Equipment.get { it.id in script.equipment.keys }.size == script.equipment.keys.size
	}


}

class HasAllItemsFromBank(script: Tempoross) : Branch<Tempoross>(script, "Has All Items") {
	override val failedComponent: TreeComponent<Tempoross> = GetItemsFromBank(script)
	override val successComponent: TreeComponent<Tempoross> = EnterBoat(script)

	override fun validate(): Boolean {
		val equipment = Equipment.stream().toList().map { it.id }
		val inventory = script.getRelevantInventoryItems()
		return script.equipment.all { equipment.contains(it.key) } && script.inventoryBankItems.all {
			inventory.getOrDefault(it.key, 0) >= it.value
		}
	}


}

class WaitingForStart(script: Tempoross) : Branch<Tempoross>(script, "Waiting For Game Start") {
	override fun validate(): Boolean {
		if (script.side == Side.UNKNOWN) {
			if (Npcs.stream().name("Ammunition crate").findFirst().isPresent) {
				script.logger.info("Getting Side of mini-game")
				val mast = Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Mast").nearest().first()
				script.logger.info("Mast found: $mast, orientation: ${mast.orientation()}")
				script.side = if (mast.orientation() == 4) Side.SOUTH else Side.NORTH
				script.side.mastLocation = mast.tile()
			} else {
				script.logger.info("Couldn't find ammunition crate")
				return true
			}
		}
		return false
	}


	override val successComponent: TreeComponent<Tempoross> = ShouldHopFromOtherPlayers(script)

	override val failedComponent: TreeComponent<Tempoross> = ShouldLeave(script)
}

class ShouldHopFromOtherPlayers(script: Tempoross) : Branch<Tempoross>(script, "Should hop?") {
	override val failedComponent: TreeComponent<Tempoross> = ShouldFillBuckets(script)
	override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Hopping") {
		val worlds = Worlds.stream().filtered { it.type() == World.Type.MEMBERS && it.population >= 15 && it.usable(script.solo) }
			.toList()
		val world = if (script.solo) worlds.random() else worlds.maxByOrNull { it.population } ?: return@SimpleLeaf
		world.hop()
	}

	private fun World.usable(solo: Boolean) = if (solo) specialty != World.Specialty.TEMPOROSS else specialty == World.Specialty.TEMPOROSS
	override fun validate(): Boolean {
		return if (script.solo) {
			script.playersReady() > 1
		} else {
			Worlds.current().specialty != World.Specialty.TEMPOROSS
		}

	}
}


class ShouldFillBuckets(script: Tempoross) : Branch<Tempoross>(script, "Should Fill Buckets") {
	override fun validate(): Boolean {
		return Inventory.containsOneOf(EMPTY_BUCKET)
	}

	override val successComponent: TreeComponent<Tempoross> = FillBuckets(script)
	override val failedComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Wait for game to start...") {
		waitFor(60000) { script.energy > -1 }
	}
}

class ShouldLeave(script: Tempoross) : Branch<Tempoross>(script, "Should leave") {
	override fun validate(): Boolean {
		return Npcs.stream().action("Leave").isNotEmpty()
	}

	override val successComponent: TreeComponent<Tempoross> = HasAllBuckets(script)
	override val failedComponent: TreeComponent<Tempoross> = ShouldTether(script)
}

class HasAllBuckets(script: Tempoross) : Branch<Tempoross>(script, "Should get buckets before leaving") {
	override val failedComponent: TreeComponent<Tempoross> = GetBuckets(script)
	override val successComponent: TreeComponent<Tempoross> = Leave(script)

	override fun validate(): Boolean {
		return script.getTotalBuckets() >= script.buckets
	}

}