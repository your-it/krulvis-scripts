package org.powbot.krulvis.api.script.tree

import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.script.ATScript

abstract class Branch<S : ATScript>(override val script: S, override val name: String) : TreeComponent<S>() {


    abstract val successComponent: TreeComponent<S>
    abstract val failedComponent: TreeComponent<S>

    abstract fun validate(): Boolean

    /**
     * Executes either successComponent or failedComponent depending on whether validate() return true or false
     */
    override fun execute() {
        val validate = validate()
        val comp = if (validate) successComponent else failedComponent
        debug("$name was ${if (validate) "successful" else "unsuccessful"}, executing: ${comp.name}")
        if (comp is Leaf) {
            script.lastLeaf = comp
        }
        comp.execute()
    }

}

class SimpleBranch<S : ATScript>(
    script: S, name: String,
    override val successComponent: TreeComponent<S>,
    override val failedComponent: TreeComponent<S>,
    val validator: () -> Boolean
) : Branch<S>(script, name) {

    override fun validate(): Boolean {
        return validator()
    }
}
