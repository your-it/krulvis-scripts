package org.powbot.krulvis.gwd.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.alive
import org.powbot.krulvis.gwd.GWDScript
import org.powbot.krulvis.gwd.leaf.Kill


class MageAlive<S>(script: S) : Branch<S>(script, "MageAlive") where S : GWDScript<S> {
    override val failedComponent: TreeComponent<S> = MeleeAlive(script)
    override val successComponent = Kill(script, true)

    override fun validate(): Boolean {
        script.mageBG = script.god.getMage()
        if (script.mageBG.alive()) {
            successComponent.target = script.mageBG
            return true
        }
        return false
    }
}

class MeleeAlive<S>(script: S) : Branch<S>(script, "MeleeAlive") where S : GWDScript<S> {
    override val failedComponent: TreeComponent<S> = HasKC(script)
    override val successComponent = Kill(script, true)

    override fun validate(): Boolean {
        script.mageBG = script.god.getMage()
        if (script.mageBG.alive()) {
            successComponent.target = script.mageBG
            return true
        }
        return false
    }
}