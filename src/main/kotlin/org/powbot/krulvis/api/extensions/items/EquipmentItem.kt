package org.powbot.krulvis.api.extensions.items

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.requirements.Requirement
import org.powerbot.script.rt4.Equipment
import java.awt.image.BufferedImage
import java.io.Serializable

interface EquipmentItem : Item {

    val requirements: List<Requirement>

    val slot: Equipment.Slot?
        get() = null

    override val image: BufferedImage?
        get() = null

    override fun hasWith(ctx: ATContext): Boolean = inInventory(ctx) || inEquipment(ctx)

    override fun getCount(ctx: ATContext, countNoted: Boolean): Int {
        return getInventoryCount(ctx, countNoted) + ctx.equipment.toStream().id(*ids).count(true).toInt()
    }

    fun withdrawAndEquip(ctx: ATContext): Boolean {
        if (inEquipment(ctx)) {
            return true
        } else if (!inInventory(ctx)) {
            if (ctx.bank.withdrawModeNoted(false) && inBank(ctx)
                && ctx.bank.withdraw(1, getBankId(ctx, true))
            ) {
                waitFor(5000) { inInventory(ctx) }
            }
        }
        if (inInventory(ctx)) {
            return equip(ctx, true)
        }
        return inEquipment(ctx)
    }

    fun inEquipment(ctx: ATContext): Boolean {
        return ctx.equipment.any { it.id() in ids }
    }

    fun canWear(ctx: ATContext): Boolean = requirements.all { it.hasRequirement(ctx) }

    fun equip(ctx: ATContext, wait: Boolean = true): Boolean {
        if (!inEquipment(ctx) && inInventory(ctx)) {
            val item = ctx.inventory.toStream().id(*ids).first()
            val action = item.actions().first { it in listOf("Wear", "Wield", "Equip") }
            if (ctx.ge.close() && item.interact(action)) {
                if (wait) waitFor(2000) { inEquipment(ctx) } else return true
            }
        }
        return inEquipment(ctx)
    }

    fun dequip(ctx: ATContext): Boolean {
        if (!inEquipment(ctx)) {
            return true
        }
        val equipped = ctx.equipment.toStream().id(*ids).first()
        return equipped.click()
    }
}

class Equipment(
    override val requirements: List<Requirement>,
    override vararg val ids: Int,
    override val slot: Equipment.Slot? = null,
    override val image: BufferedImage? = null
) : EquipmentItem, Serializable