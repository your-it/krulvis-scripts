package org.powbot.krulvis.mta.tree.branches

import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.AlchemyRoom
import org.powbot.krulvis.mta.tree.leafs.CastHighAlch
import org.powbot.krulvis.mta.tree.leafs.SearchCupboard

class CanCastHA(script: MTA) : Branch<MTA>(script, "Can Cast HA?") {
	override val failedComponent: TreeComponent<MTA> = IsItemOnGround(script)
	override val successComponent: TreeComponent<MTA> = CastHighAlch(script)

	var lastBest = ""
	override fun validate(): Boolean {
		AlchemyRoom.bestItemName = AlchemyRoom.getBestItem().itemName

		script.log.info("Best item = ${AlchemyRoom.bestItemName}, changed=${lastBest != AlchemyRoom.bestItemName}")
		if (lastBest != AlchemyRoom.bestItemName) {
			AlchemyRoom.EMPTY_CUPBOARD = -1
			lastBest = AlchemyRoom.bestItemName
		}
		return Inventory.stream().name(AlchemyRoom.bestItemName).isNotEmpty()
	}
}

class IsItemOnGround(script: MTA) : Branch<MTA>(script, "Is item on ground?") {
	override val failedComponent: TreeComponent<MTA> = ShouldDrop(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Picking groundItem") {
		if (groundItem.interact("Take")) {
			waitForDistance(groundItem) { Inventory.stream().name(AlchemyRoom.bestItemName).isNotEmpty() }
		}
	}

	var groundItem: GroundItem = GroundItem.Nil

	override fun validate(): Boolean {
		groundItem = GroundItems.stream().name(AlchemyRoom.bestItemName).first()
		return groundItem.valid() && !Inventory.isFull()
	}
}

class ShouldDrop(script: MTA) : Branch<MTA>(script, "Should drop items?") {
	override val failedComponent: TreeComponent<MTA> = SearchCupboard(script)
	override val successComponent: TreeComponent<MTA> = SimpleLeaf(script, "Dropping") {
//        val count = Inventory.stream().name(droppable.first().name()).count()
		droppables.forEach { it.interact("Drop") }
		waitFor(600) { AlchemyRoom.getDroppables().isEmpty() }
	}

	var droppables: List<Item> = emptyList()

	override fun validate(): Boolean {
		droppables = AlchemyRoom.getDroppables()
		script.log.info("droppables=${droppables.count()}")
		return droppables.isNotEmpty()
	}
}