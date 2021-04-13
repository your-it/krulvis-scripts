package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powerbot.script.rt4.Bank


class InventoryRequirement(
    override val item: Item,
    override val amount: Int,
    private val onlyBest: Boolean = false,
    val allowMore: Boolean = false,
    val countNoted: Boolean = false
) : ItemRequirement {

    constructor(id: Int, amount: Int, allowMore: Boolean = false, countNoted: Boolean = false) : this(object : Item {
        override val ids: IntArray
            get() = intArrayOf(id)

        override fun hasWith(ctx: ATContext): Boolean {
            return inInventory(ctx)
        }

        override fun getCount(ctx: ATContext, countNoted: Boolean): Int {
            return ctx.inventory.toStream().id(this.id).count().toInt()
        }
    }, amount, false, allowMore, countNoted)

    fun getCount(ctx: ATContext): Int {
        return (if (onlyBest) ctx.inventory.toStream().id(item.id).count(true).toInt()
        else item.getInventoryCount(ctx, countNoted))
    }

    override fun hasRequirement(ctx: ATContext): Boolean {
        return if (allowMore) getCount(ctx) >= amount else getCount(ctx) == amount
    }

    override fun withdraw(ctx: ATContext, wait: Boolean): Boolean {
        val count = getCount(ctx)
        val id = item.getBankId(ctx, true)
        if (count == amount) {
            return true
        } else if (id == -1) {
            return false
        } else if (onlyBest) {
            return ctx.withdrawExact(item.id, amount, wait)
        } else {
            if (count > amount) {
                item.ids.forEach { ctx.bank.deposit(it, Bank.Amount.ALL) }
                if (wait) waitFor { !item.inInventory(ctx) }
            } else if (count < amount) {
                return if (allowMore && amount == 99) {
                    ctx.bank.withdraw(id, Bank.Amount.ALL)
                } else
                    item.withdrawExact(ctx, amount, false, wait)
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
                "InvReq: ${item.id}: $amount"
            }
        }
    }
}