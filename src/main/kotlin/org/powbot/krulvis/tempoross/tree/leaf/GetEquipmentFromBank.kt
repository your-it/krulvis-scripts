package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearest
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

class GetEquipmentFromBank(script: Tempoross) : Leaf<Tempoross>(script, "Getting Equipment") {
	override fun execute() {
		getEquipablesInInventory().forEach { it.equip() }
		val missingEquipment = getMissingEquipment()

		if (Bank.openNearest()) {
			missingEquipment.forEach { (id, amount) ->
				if (!Bank.withdrawExact(id, amount) && !Bank.containsOneOf(id)) {
					Notifications.showNotification("Missing equipment ${ItemLoader.lookup(id)?.name() ?: id}")
					ScriptManager.stop()
					return@forEach
				}
			}
		}

	}

	private val EQUIP_ACTIONS = listOf("Wear", "Wield")
	private fun Item.equip(): Boolean {
		val action = actions().firstOrNull { it in EQUIP_ACTIONS } ?: return false
		return interact(action) && waitFor { Equipment.stream().id(id).isNotEmpty() }
	}

	private fun getMissingEquipment(): Map<Int, Int> {
		val equipped = Equipment.stream().map { it.id }
		val inventory = Inventory.stream().toList().map { it.id }
		return script.equipment.filterNot { equipped.contains(it.key) || inventory.contains(it.key) }.map { it.key to 1 }.toMap()
	}

	private fun getEquipablesInInventory(): List<Item> {
		return Inventory.get { it.id in script.equipment.keys }
	}
}