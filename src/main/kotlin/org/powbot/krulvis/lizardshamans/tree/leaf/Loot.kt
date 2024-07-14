package org.powbot.krulvis.lizardshamans.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.lizardshamans.LizardShamans

class Loot(script: LizardShamans) : Leaf<LizardShamans>(script, "Looting") {
	override fun execute() {
		val loots = script.lootList.sortedWith(compareBy<GroundItem> { it.distance() }
			.thenByDescending { GrandExchange.getItemPrice(it.id()) * it.stackSize() })
		script.logger.info("Looting=[${loots.joinToString()}]")

		loots.forEachIndexed { i, gi ->
			if (Inventory.isFull() && !gi.canFit()) {
				Food.getFirstFood()?.eat()
			}
			val id = gi.id()
			val currentCount = Inventory.getCount(id)
			if (walkAndInteract(gi, "Take") && (i == loots.size - 1 || gi.distance() >= 1)) {
				waitFor(5000) { currentCount < Inventory.getCount(gi.id()) || Players.local().tile() == gi.tile }
			}
		}
		script.lootList.removeAll { loot ->
			GroundItems.stream().at(loot.tile).name(loot.name()).none { gi -> gi.stackSize() == loot.stackSize() }
		}
		script.logger.info("Remaining loot=[${script.lootList.joinToString()}]")
	}


	fun GenericItem.canFit(): Boolean = stackable() && Inventory.getCount(id()) > 0
}