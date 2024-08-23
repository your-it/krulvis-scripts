package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.stripBarrowsCharge
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

class EquipmentItem(override val id: Int, override val slot: Equipment.Slot) : IEquipmentItem {
	override val ids: IntArray = intArrayOf(id)
	override val name: String by lazy { ItemLoader.lookup(id)!!.name().stripBarrowsCharge() }
	override val stackable: Boolean by lazy { ItemLoader.lookup(id)!!.stackable() }
}

interface IEquipmentItem : Item {

	val slot: Equipment.Slot

	override fun hasWith(): Boolean = inInventory() || inEquipment()

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(false) + getEquipmentCount()
	}

	fun withdrawAndEquip(stopIfOut: Boolean = false): Boolean {
		if (inEquipment()) {
			return true
		} else if (!inInventory()) {
			if (Bank.withdrawModeNoted(false) && inBank()
				&& withdrawExact(1, true)
			) {
				waitFor(5000) { inInventory() }
			} else if (!inBank() && stopIfOut) {
				ScriptManager.script()!!.logger.warn("Stopping script due to being out of: $name")
				ScriptManager.stop()
			}
		}
		if (inInventory()) {
			return equip(true)
		}
		return inEquipment()
	}

	fun equip(wait: Boolean = true): Boolean {
		if (inInventory()) {
			val item = Inventory.stream().nameContains(name).first()
			val actions = item.actions()
			val action = actions.firstOrNull { it in listOf("Wear", "Wield", "Equip") }
			ScriptManager.script()!!.logger.info("Equipping $name action=$action, actions=[${actions.joinToString()}]")
			if (if (action == null) item.click() else item.interact(action)) {
				if (wait) waitFor(2000) { inEquipment() } else return true
			}
		}
		return inEquipment()
	}

	fun dequip(): Boolean {
		if (!inEquipment()) {
			return true
		}
		val equipped = Equipment.stream().nameContains(name).first()
		return equipped.click()
	}
}