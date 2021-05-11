package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.extensions.items.Item

interface ItemRequirement : Requirement {

    val item: Item
    val amount: Int

    /**
     * Handles banking
     */
    fun withdraw(wait: Boolean): Boolean
}