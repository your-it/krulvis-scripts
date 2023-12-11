package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross

class GetEquipment(script: Tempoross) : Leaf<Tempoross>(script, "Getting Equipment") {
    override fun execute() {
        val missingEquipment = getMissingEquipment()
        if (missingEquipment.isEmpty()) {
            equipEquipment()
            waitFor { script.equipment.all { Equipment.stream().id(it.key).isNotEmpty() } }
        } else if (!Bank.opened()) {
            Bank.openNearest()
        } else {
            withdrawEquipment(missingEquipment)
        }
    }

    private fun equipEquipment(): Boolean {
        if (Bank.opened()) {
            Bank.close()
        } else {
            val inventoryEquipment = Inventory.stream().id(*script.equipment.map { it.key }.toIntArray()).toList()
            return inventoryEquipment.all { it.click() }
        }
        return false
    }

    private fun getMissingEquipment(): List<Int> {
        val equipped = Equipment.stream().map { it.id }
        val inventory = Inventory.stream().toList().map { it.id }
        return script.equipment.filterNot { equipped.contains(it.key) || inventory.contains(it.key) }.map { it.key }
    }

    private fun withdrawEquipment(missingEquipment: List<Int>): Boolean {
        return missingEquipment.all { Bank.withdrawExact(it, 1) }
    }
}