package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter

class Loot(script: Fighter) : Leaf<Fighter>(script, "Looting") {
	override fun execute() {
		if (script.canDeactivateQuickPrayer()) {
			script.logger.info("Deactivating prayer, using safespot=${script.useSafespot}, healthBarVisible=${me.healthBarVisible()}")
			Prayer.quickPrayer(false)
		}

		val loots = script.loot().sortedWith(compareBy<GroundItem> { it.distance() }
			.thenByDescending { GrandExchange.getItemPrice(it.id()) * it.stackSize() })
		script.logger.info("Looting=[${loots.joinToString()}]")

		loots.forEachIndexed { i, gi ->
			val id = gi.id()
			val currentCount = Inventory.getCount(id)
			if ((!Inventory.isFull()
					|| gi.stackable()
					|| (Inventory.containsOneOf(Item.HERB_SACK_OPEN) && gi.name().contains("grimy", true)))
				&& walkAndInteract(gi, "Take") && (i == loots.size - 1 || gi.distance() >= 1)
			) {
				waitFor(5000) { currentCount < Inventory.getCount(gi.id()) || Players.local().tile() == gi.tile }
			}
		}
		script.lootList.removeAll { loot ->
			GroundItems.stream().at(loot.tile).name(loot.name()).none { gi -> gi.stackSize() == loot.stackSize() }
		}
		script.logger.info("Remaining loot=[${script.lootList.joinToString()}]")
	}
}