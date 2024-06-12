package org.powbot.krulvis.combiner.interactions

import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item

object InventoryActions {
	fun InventoryItemActionEvent.getItem(): Item? {
		val itemName = if (name.contains("->")) name.substring(name.indexOf("-> ") + 3) else name
		return Inventory.stream().name(itemName).firstOrNull()
	}
}