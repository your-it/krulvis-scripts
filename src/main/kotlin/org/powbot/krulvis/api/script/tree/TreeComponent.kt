package org.powbot.krulvis.api.script.tree

import org.powbot.krulvis.api.ATContext

abstract class TreeComponent: ATContext {


    /**
     * Leave this be, used in the TreeScript to update the logic
     */
    abstract fun execute()

    /**
     * Reset stored components whenever required
     */
    abstract fun reset()

    abstract fun name(): String
}