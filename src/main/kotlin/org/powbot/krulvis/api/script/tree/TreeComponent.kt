package org.powbot.krulvis.api.script.tree

import org.powbot.krulvis.api.script.ATScript
import org.powerbot.script.rt4.ClientContext

abstract class TreeComponent<S : ATScript> {

    abstract val script: S

    val ctx: ClientContext get() = script.ctx

    /**
     * Leave this be, used in the TreeScript to update the logic
     */
    abstract fun execute()

    /**
     * Name of the TreeComponent for debugging purposes
     */
    abstract val name: String

    override fun toString(): String {
        return name
    }
}
