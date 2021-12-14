package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.Notifications
import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.extensions.items.Item.Companion.JUG
import org.powbot.krulvis.api.extensions.items.Item.Companion.PIE_DISH
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.api.Random
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.mobile.script.ScriptManager

class ShouldStop(script: Fighter) : Branch<Fighter>(script, "Should stop?") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Stopping") {
        Notifications.showNotification("Stopped because task was finished!")
        ScriptManager.stop()
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldSipPotion(script, ShouldEquipAmmo(script))

    override fun validate(): Boolean {
        return script.lastTask && script.taskRemainder() <= 0
    }
}

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
            if (item.interact("Bury")) {
                waitFor { count > Inventory.getCount(item.id) }
                if (i < bones.size - 1)
                    sleep(1500)
            }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldExitRoom(script)

    override fun validate(): Boolean {
        if (!script.buryBones) return false
        bones = Inventory.stream().filtered { it.name().contains("bones", true) }.list()
        return bones.isNotEmpty()
    }
}