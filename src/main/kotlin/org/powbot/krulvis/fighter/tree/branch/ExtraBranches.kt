package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter


class ShouldEat(script: Fighter) : Branch<Fighter>(script, "Should eat?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Eating") {
        val count = script.food?.getInventoryCount() ?: -1
        if (script.food?.eat() == true) {
            waitFor { script.food!!.getInventoryCount() < count }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldEquipAmmo(script)

    override fun validate(): Boolean {
        return script.food?.inInventory() == true && (script.needFood() || script.canEat())
    }
}

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
        script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.equip()
        waitFor { script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.inInventory() != true }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldBank(script)

    override fun validate(): Boolean {
        val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
        return ammo != null && ammo.inInventory()
                && (Inventory.isFull()
                || (ammo.getInvItem()?.stack ?: -1) > 5
                || !ammo.inEquipment()
                )
    }
}