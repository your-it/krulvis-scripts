package org.powbot.krulvis.api.script.tree

import org.powbot.krulvis.api.script.ATScript

abstract class Leaf<S : ATScript>(override val script: S, override val name: String) : TreeComponent<S>()

class SimpleLeaf<S : ATScript>(script: S, name: String, val action: () -> Unit) :
    Leaf<S>(script, name) {
    override fun execute() {
        action()
    }
}
