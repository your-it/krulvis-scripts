package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.EquipmentItem


class EquipmentRequirement(override val item: EquipmentItem, override val amount: Int = 1) : ItemRequirement {

    override fun withdraw(ctx: ATContext, wait: Boolean): Boolean {
        if (item.getCount(ctx) == amount) {
            return true
        }
        return ctx.withdrawExact(item.id,amount - ctx.equipment.toStream().id(*item.ids).count(true), wait)
    }

    override fun hasRequirement(ctx: ATContext): Boolean {
        return item.inEquipment(ctx)
    }
}