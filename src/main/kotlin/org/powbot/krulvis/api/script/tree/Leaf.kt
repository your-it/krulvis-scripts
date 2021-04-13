package org.powbot.krulvis.api.script.tree

import org.powbot.krulvis.api.script.ATScript


abstract class Leaf<out S : ATScript>(override val script: S, private val name: String?) :
    TreeComponent() {

    constructor(script: S) : this(script, null)

    override fun name(): String = this.name ?: javaClass.simpleName

    /**
     * Override update to make sure current leaf is set for debugging
     */
    override fun execute() {
        if (script.debugComponents) {
            println("${this.name()} executed")
        }
        script.lastLeaf = this
        loop()
    }

    abstract fun loop()

    override fun reset() {
        //Doesn't have to do anything
    }

    override fun toString(): String {
        return name()
    }
}

class EmptyLeaf(script: ATScript, name: String) : Leaf<ATScript>(script, name) {

    constructor(script: ATScript) : this(script, "EmptyLeaf")

    override fun loop() {
        //DO nothing at all
    }

}