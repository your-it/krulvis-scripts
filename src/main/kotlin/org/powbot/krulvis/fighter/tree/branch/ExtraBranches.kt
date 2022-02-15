package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Item.Companion.JUG
import org.powbot.krulvis.api.extensions.items.Item.Companion.PIE_DISH
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Loot

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
        script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.equip()
        waitFor { script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.inInventory() != true }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script, ShouldDropTrash(script))

    override fun validate(): Boolean {
        val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
        return ammo != null && ammo.inInventory()
                && (Inventory.isFull()
                || (ammo.getInvItem()?.stack ?: -1) > 5
                || !ammo.inEquipment()
                )
    }
}

class ShouldDropTrash(script: Fighter) : Branch<Fighter>(script, "Should Drop Trash?") {

    val TRASH = intArrayOf(VIAL, PIE_DISH, JUG)
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Dropping vial") {
        if (Inventory.stream().id(*TRASH).firstOrNull()?.interact("Drop") == true)
            waitFor { Inventory.stream().id(*TRASH).firstOrNull() == null }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldBuryBones(script)

    override fun validate(): Boolean {
        return Inventory.stream().id(*TRASH).firstOrNull() != null
    }
}

class ShouldBuryBones(script: Fighter) : Branch<Fighter>(script, "Should Bury bones?") {

    var bones = emptyList<Item>()

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Bury bones") {
        bones.forEachIndexed { i, item ->
            val count = Inventory.getCount(item.id)
            val action = item.actions().first { action -> actions.any { it.equals(action, true) } }
            if (item.interact(action)) {
                waitFor { count > Inventory.getCount(item.id) }
                if (i < bones.size - 1)
                    sleep(1500)
            }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldBank(script)

    val names = arrayOf("bones", "ashes")
    val actions = arrayOf("burry", "scatter")

    override fun validate(): Boolean {
        if (!script.buryBones) return false
        bones = Inventory.stream().filtered { item ->
            val name = item.name().lowercase()
            names.any { name.contains(it) }
        }.list()
        return bones.isNotEmpty()
    }
}


///BANKING SITS IN BETWEEN HERE

class CanLoot(script: Fighter) : Branch<Fighter>(script, "Can loot?") {
    override val successComponent: TreeComponent<Fighter> = Loot(script)
    override val failedComponent: TreeComponent<Fighter> = ShouldExitRoom(script)

    override fun validate(): Boolean {
        val loot = script.loot()
        if (loot.isEmpty() || !loot.first().reachable()) {
            return false
        }
        return !Inventory.isFull() || Food.hasFood() || loot.any { it.stackable() && Inventory.containsOneOf(it.id()) }
    }
}