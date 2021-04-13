package org.powbot.krulvis.api.script.tree

import org.powbot.krulvis.api.script.ATScript
import org.powerbot.script.rt4.ClientContext

abstract class Branch<out S : ATScript>(override val script: S) :
    TreeComponent() {

    open val name: String = javaClass.simpleName

    override fun name() = name

    abstract fun validate(): Boolean

    var successComponent: TreeComponent? = null
    var failedComponent: TreeComponent? = null

    /**
     * No logic is allowed in the onSuccess() / onFailure() component. It needs to return an instance of a TreeComponent which will be stored
     */
    abstract fun onSuccess(): TreeComponent

    abstract fun onFailure(): TreeComponent

    override fun execute() {
        if (validate()) {
            debug("$name successful")
            if (successComponent == null) {
                successComponent = onSuccess()
            }
            successComponent?.execute()
        } else {
            debug("$name failed")
            if (failedComponent == null) {
                failedComponent = onFailure()
            }
            failedComponent?.execute()
        }
    }

    override fun reset() {
        successComponent = null
        failedComponent = null
    }
}

class SimpleBranch<out S : ATScript>(
    script: S,
    override val name: String = "SimpleBranch",
    val validator: () -> Boolean,
    val onSuccess: TreeComponent,
    val onFailure: TreeComponent
) : Branch<S>(script) {

    override fun validate(): Boolean {
        return validator()
    }

    override fun onSuccess(): TreeComponent {
        return onSuccess
    }

    override fun onFailure(): TreeComponent {
        return onFailure
    }

}