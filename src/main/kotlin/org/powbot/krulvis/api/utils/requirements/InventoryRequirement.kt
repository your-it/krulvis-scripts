package org.powbot.krulvis.api.utils.requirements

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.InventoryItem
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportEquipment


open class InventoryRequirement(
	override val item: Item,
	override val amount: Int,
	private val onlyBest: Boolean = false,
	val allowMore: Boolean = false,
	val countNoted: Boolean = true
) : ItemRequirement {

	constructor(id: Int, amount: Int, allowMore: Boolean = false, countNoted: Boolean = true) : this(
		Potion.forId(id) ?: TeleportEquipment.getTeleportItem(id) ?: InventoryItem(id),
		amount, false, allowMore, countNoted
	)

	fun getCount(): Int {
		return (if (onlyBest) Inventory.stream().id(item.id).count(countNoted).toInt()
		else item.getInventoryCount(countNoted))
	}

	override fun meets(): Boolean {
		return if (allowMore) getCount() >= amount else getCount() == amount
	}

	override fun withdraw(wait: Boolean): Boolean {
		return item.withdrawExact(amount, wait = wait)
	}

	override fun toString(): String {
		return when (item) {
//            is Potion -> {
//                "InvReq: ${item.name}: $amount"
//            }
//            is TeleportItem -> {
//                "InvReq: ${item.itemName}: $amount"
//            }
			else -> {
				"InventoryRequirement(name=${item.id}, amount=$amount)"
			}
		}
	}


	companion object {
		fun forOption(option: Map<Int, Int>) = option.map { InventoryRequirement(it.key, it.value, allowMore = it.value > 28) }
	}
}