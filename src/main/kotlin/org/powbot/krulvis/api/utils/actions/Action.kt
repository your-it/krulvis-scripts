package org.powbot.krulvis.api.utils.actions

import org.powbot.krulvis.api.ATContext

interface Action {

    /**
     * Execute the Action
     */
    fun execute(ctx: ATContext): Boolean

}