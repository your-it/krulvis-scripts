package org.powbot.krulvis.api.extensions.requirements

import org.powbot.api.requirement.Requirement
import org.powbot.krulvis.api.extensions.items.Item

interface ItemRequirement : Requirement {

	val item: Item
	val amount: Int

	/**
	 * Handles banking
	 */
	fun withdraw(wait: Boolean): Boolean
}