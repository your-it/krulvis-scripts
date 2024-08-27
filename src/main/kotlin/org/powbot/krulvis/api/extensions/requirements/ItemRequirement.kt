package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.ITeleportItem
import org.powbot.krulvis.api.extensions.items.Item

interface ItemRequirement : Requirement {

	val item: Item
	var amount: Int

	/**
	 * Handles banking
	 */
	fun withdraw(wait: Boolean): Boolean {
		if (item is ITeleportItem) {
			item.withdrawExact(amount, true)
		}
		return item.withdrawExact(amount, wait = wait)
	}
}