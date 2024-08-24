package org.powbot.krulvis.tormenteddemon.tree.branch

import org.powbot.api.rt4.GrandExchange
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Item.Companion.HERB_SACK_OPEN
import org.powbot.krulvis.api.extensions.items.Item.Companion.SEED_BOX_OPEN
import org.powbot.krulvis.tormenteddemon.TormentedDemon
import org.powbot.krulvis.tormenteddemon.tree.leaf.Loot
import kotlin.math.max

class CanLoot(script: TormentedDemon) : Branch<TormentedDemon>(script, "Can loot?") {
	override val successComponent: TreeComponent<TormentedDemon> = Loot(script)
	override val failedComponent: TreeComponent<TormentedDemon> = ShouldDodgeProjectile(script)

	private fun makeSpace(worth: Int): Boolean {
		val edibleFood = Food.getFirstFood()
		if (edibleFood != null) {
			return edibleFood.eat()
		} else if (worth > 5000) {
			val pot = Inventory.stream().name("(1)").action("Drink").first()
			return pot.interact("Drink")
		}
		return false
	}

	override fun validate(): Boolean {
		val loot = script.lootList.map { it to it.stackSize() * max(GrandExchange.getItemPrice(it.id()), it.price()) }
		if (loot.isEmpty() || !loot.first().first.reachable()) {
			return false
		}
		val lootString = loot.joinToString { it.first.name() + ": " + it.second }
		script.logger.info("Loot found: [${lootString}]")
		if (!Inventory.isFull()) return true

		val worth = loot.sumOf { it.second }
		val hasHerbSack = Inventory.containsOneOf(HERB_SACK_OPEN)
		val hasSeedBox = Inventory.containsOneOf(SEED_BOX_OPEN)
		return loot.any { it.first.stacks(hasHerbSack, hasSeedBox) } || makeSpace(worth)
	}

	private fun GroundItem.stacks(herbSack: Boolean, seedBox: Boolean): Boolean {
		return stackable() && Inventory.containsOneOf(id())
			|| (herbSack && name.contains("grimy", true))
			|| (seedBox && name.contains("seed", true))
	}
}

