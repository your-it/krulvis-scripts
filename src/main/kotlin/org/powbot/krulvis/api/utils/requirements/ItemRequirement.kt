package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.utils.requirements.Requirement

interface ItemRequirement : Requirement {

    val item: Item
    val amount: Int

    /**
     * Handles banking
     */
    fun withdraw(ctx: ATContext, wait: Boolean): Boolean

    fun toString(ctx: ATContext): String = "${item.name(ctx)}: $amount"
}