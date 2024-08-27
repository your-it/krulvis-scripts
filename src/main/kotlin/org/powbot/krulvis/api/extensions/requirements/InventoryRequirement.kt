package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.ITeleportItem
import org.powbot.krulvis.api.extensions.items.InventoryItem
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Potion


open class InventoryRequirement(
	override val item: Item,
	override var amount: Int,
	private val onlyBest: Boolean = false,
	val allowMore: Boolean = false,
	val countNoted: Boolean = true
) : ItemRequirement {

	constructor(id: Int, amount: Int, allowMore: Boolean = false, countNoted: Boolean = true) : this(
		Potion.forId(id) ?: ITeleportItem.getTeleportItem(id) ?: InventoryItem(id),
		amount, false, allowMore, countNoted
	)


	fun getCount(): Int {
		return (if (onlyBest) Inventory.stream().id(item.id).count(countNoted).toInt()
		else item.getInventoryCount(countNoted))
	}

	override fun meets(): Boolean {
		if (item is Potion) {
			item.getInventoryCount()
		}
		return if (allowMore) getCount() >= amount else getCount() == amount
	}

	override fun toString(): String =
		"InventoryRequirement(name=${item.itemName}, amount=$amount, type=${item.javaClass.simpleName})"


	companion object {
		fun forOption(option: Map<Int, Int>) =
			option.map { InventoryRequirement(it.key, it.value, allowMore = it.value > 28) }
	}
}