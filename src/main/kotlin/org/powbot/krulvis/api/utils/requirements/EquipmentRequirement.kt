package org.powbot.krulvis.api.utils.requirements

import org.powbot.api.rt4.Equipment
import org.powbot.krulvis.api.extensions.items.EquipmentItem
import org.powbot.krulvis.api.extensions.items.IEquipmentItem


class EquipmentRequirement(override val item: IEquipmentItem, override val amount: Int = 1) : ItemRequirement {

	val slot = item.slot

	constructor(id: Int, slot: Equipment.Slot, amount: Int = 1) : this(EquipmentItem(id, slot), amount)
	constructor(id: Int, slot: Int, amount: Int = 1) : this(EquipmentItem(id, Equipment.Slot.forIndex(slot)!!), amount)

	override fun withdraw(wait: Boolean): Boolean {
		return item.withdrawExact(amount)
	}

	fun withdrawAndEquip(stopIfOut: Boolean): Boolean {
		return item.withdrawAndEquip(stopIfOut)
	}

	override fun meets(): Boolean {
		return item.inEquipment()
	}

	override fun toString(): String = "EquipmentRequirement -> ${item.id}: $amount"

	companion object {
		fun forEquipmentOption(option: Map<Int, Int>): List<EquipmentRequirement> {
			return option.map { EquipmentRequirement(it.key, it.value) }
		}

		fun List<EquipmentRequirement>.withdrawAndEquip(): Boolean {
			val missing = filterNot { it.meets() }
			if (missing.isEmpty()) return true
			return missing.all { it.withdrawAndEquip(true) }
		}

		fun List<EquipmentRequirement>.ids() = flatMap { it.item.ids.toList() }.toIntArray()
		fun List<EquipmentRequirement>.names() = map { it.item.name }
	}
}