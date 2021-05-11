package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.items.EquipmentItem


class EquipmentRequirement(override val item: EquipmentItem, override val amount: Int = 1) : ItemRequirement {

    override fun withdraw(wait: Boolean): Boolean {
        if (item.getCount() == amount) {
            return true
        }
        return withdrawExact(item.id, amount - ctx.equipment.toStream().id(*item.ids).count(true), wait)
    }

    override fun hasRequirement(): Boolean {
        return item.inEquipment()
    }

    override fun toString(): String = "EquipmentRequirement -> ${item.itemName()}: $amount"
}