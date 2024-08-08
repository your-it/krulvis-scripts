package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.EquipmentItem
import org.powbot.krulvis.api.extensions.items.IEquipmentItem
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.fighter.Fighter

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
		invAmmo.interact("Equip")
		Utils.waitFor { !validate() }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script, ShouldEquipGloves(script))

	var invAmmo = Item.Nil
	override fun validate(): Boolean {
		val ammo = script.ammo ?: return false
		invAmmo = ammo.item.getInvItem() ?: return false
		return invAmmo.valid() && (
			Inventory.isFull()
				|| invAmmo.stack > 5
				|| !ammo.item.inEquipment()
			)
	}
}

class ShouldEquipGloves(script: Fighter) : Branch<Fighter>(script, "Should equip gloves?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip gloves") {
		if (gloves.equip()) {
			Utils.waitFor { gloves.inEquipment() }
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldEquipGear(script)

	var gloves: IEquipmentItem = EquipmentItem(-1, Equipment.Slot.HANDS)
	override fun validate(): Boolean {
		gloves = script.equipment.firstOrNull { it.slot == Equipment.Slot.HANDS }?.item ?: return false
		if (gloves.inEquipment() || !gloves.inInventory()) return false
		return !script.currentTarget.valid() || (script.currentTarget.healthBarVisible() && script.currentTarget.healthPercent() > 10)
	}
}

class ShouldEquipGear(script: Fighter) : Branch<Fighter>(script, "Should equip gear?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip gear") {
		script.equipment.forEach { it.item.equip(false) }
		equipTimer.reset()
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldDropTrash(script)

	val equipTimer = Timer(600)
	override fun validate(): Boolean {
		return equipTimer.isFinished() && script.currentTarget.valid() && script.equipment.any { !it.meets() }
	}
}