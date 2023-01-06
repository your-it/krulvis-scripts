package org.powbot.krulvis.api.utils.requirements

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.utils.Utils.waitFor


class InventoryRequirement(
    override val item: Item,
    override val amount: Int,
    private val onlyBest: Boolean = false,
    val allowMore: Boolean = false,
    val countNoted: Boolean = true
) : ItemRequirement {

    constructor(id: Int, amount: Int, allowMore: Boolean = false, countNoted: Boolean = true) : this(object : Item {
        override val ids: IntArray
            get() = intArrayOf(id)

        override fun hasWith(): Boolean {
            return inInventory()
        }

        override fun getCount(countNoted: Boolean): Int {
            return Inventory.stream().id(this.id).count(countNoted).toInt()
        }
    }, amount, false, allowMore, countNoted)

    fun getCount(): Int {
        return (if (onlyBest) Inventory.stream().id(item.id).count(countNoted).toInt()
        else item.getInventoryCount(countNoted))
    }

    override fun meets(): Boolean {
        return if (allowMore) getCount() >= amount else getCount() == amount
    }

    override fun withdraw(wait: Boolean): Boolean {
        val count = getCount()
        val id = item.getBankId(true)
        if (count == amount) {
            return true
        } else if (id == -1) {
            return false
        } else if (onlyBest) {
            return Bank.withdrawExact(item.id, amount, wait)
        } else {
            if (count > amount) {
                item.ids.forEach { Bank.deposit(it, Bank.Amount.ALL) }
                if (wait) waitFor { !item.inInventory() }
            } else if (count < amount) {
                return if (allowMore && amount == 99) {
                    Bank.withdraw(id, Bank.Amount.ALL)
                } else
                    item.withdrawExact(amount, false, wait)
            }
        }
        return false
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
}