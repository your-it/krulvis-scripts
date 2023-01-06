package org.powbot.krulvis.api.utils.requirements

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.items.EquipmentItem


class EquipmentRequirement(override val item: EquipmentItem, override val amount: Int = 1) : ItemRequirement {

    override fun withdraw(wait: Boolean): Boolean {
        if (item.getCount() == amount) {
            return true
        }
        return Bank.withdrawExact(item.id, (amount - Equipment.stream().id(*item.ids).count(true)).toInt(), wait)
    }

    override fun meets(): Boolean {
        return item.inEquipment()
    }

    override fun toString(): String = "EquipmentRequirement -> ${item.id}: $amount"
}