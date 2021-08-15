package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.GrandExchange
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.requirements.Requirement
import java.awt.image.BufferedImage
import java.io.Serializable

interface EquipmentItem : Item {

    val requirements: List<Requirement>

    val slot: Equipment.Slot?
        get() = null

    override val image: BufferedImage?
        get() = null

    override fun hasWith(): Boolean = inInventory() || inEquipment()

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted) + Equipment.stream().id(*ids).count(true).toInt()
    }

    fun withdrawAndEquip(): Boolean {
        if (inEquipment()) {
            return true
        } else if (!inInventory()) {
            if (Bank.withdrawModeNoted(false) && inBank()
                && Bank.withdraw(1, getBankId(true))
            ) {
                waitFor(5000) { inInventory() }
            }
        }
        if (inInventory()) {
            return equip(true)
        }
        return inEquipment()
    }

    fun inEquipment(): Boolean {
        return Equipment.get().any { it.id() in ids }
    }

    fun canWear(): Boolean = requirements.all { it.hasRequirement() }

    fun equip(wait: Boolean = true): Boolean {
        if (!inEquipment() && inInventory()) {
            val item = Inventory.stream().id(*ids).first()
            val action = item.actions().first { it in listOf("Wear", "Wield", "Equip") }
            if (GrandExchange.close() && item.interact(action)) {
                if (wait) waitFor(2000) { inEquipment() } else return true
            }
        }
        return inEquipment()
    }

    fun dequip(): Boolean {
        if (!inEquipment()) {
            return true
        }
        val equipped = Equipment.stream().id(*ids).first()
        return equipped.click()
    }
}

class Equipment(
    override val requirements: List<Requirement>,
    override vararg val ids: Int,
    override val slot: org.powbot.api.rt4.Equipment.Slot? = null,
    override val image: BufferedImage? = null
) : EquipmentItem, Serializable