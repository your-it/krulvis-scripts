package org.powbot.krulvis.api.extensions.requirements

import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Potion

class PotionRequirement(val potion: Potion, amount: Int) : InventoryRequirement(potion, amount) {
	override val item: Item
		get() = potion

	override fun withdraw(wait: Boolean): Boolean {
		return potion.withdrawExact(1, worse = true)
	}

	override fun meets(): Boolean {
		return potion.inInventory()
	}
}