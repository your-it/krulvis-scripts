package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.Utils
import org.powbot.krulvis.fighter.Fighter

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
		invAmmo.interact("Equip")
		Utils.waitFor { !validate() }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script, ShouldEquipGear(script))

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


class ShouldEquipGear(script: Fighter) : Branch<Fighter>(script, "Should equip gear?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip gear") {
		script.equipment.forEach { it.item.equip(false) }
		equipTimer.reset()
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldDropTrash(script)

	val equipTimer = Timer(600)
	override fun validate(): Boolean {
		return equipTimer.isFinished() && script.currentTarget.healthPercent() > 50 && script.equipment.any { !it.meets() }
	}
}