package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Potion

class PotionRequirement(val potion: Potion) : ItemRequirement {
    override val item: Item
        get() = potion
    override val amount: Int = 1

    override fun withdraw(wait: Boolean): Boolean {
        return potion.withdrawExact(1, worse = true)
    }

    override fun meets(): Boolean {
        return potion.inInventory()
    }
}