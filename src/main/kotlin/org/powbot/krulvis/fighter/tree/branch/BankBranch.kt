package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.HandleBank
import org.powbot.krulvis.fighter.tree.leaf.OpenBank

class ShouldBank(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
	override val successComponent: TreeComponent<Fighter> = IsBankOpen(script)
	override val failedComponent: TreeComponent<Fighter> = CanLoot(script)

	override fun validate(): Boolean {
		if (script.forcedBanking) return true

		val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
		if (ammo != null && !ammo.inEquipment()) return true

		return !Food.hasFood() && (
			Food.needsFood() || Bank.opened() || (Inventory.isFull() && !Potion.PRAYER.hasWith())
			)
	}

}

class IsBankOpen(script: Fighter) : Branch<Fighter>(script, "Is Bank Open") {
	override val successComponent: TreeComponent<Fighter> = HandleBank(script)
	override val failedComponent: TreeComponent<Fighter> = OpenBank(script)
	override fun validate(): Boolean {
		return Bank.opened()
	}
}