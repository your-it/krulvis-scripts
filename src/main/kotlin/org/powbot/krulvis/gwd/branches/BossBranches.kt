package org.powbot.krulvis.gwd.branches

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.alive
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.gwd.GWDScript

class InsideGeneralRoom<S>(script: S) : Branch<S>(script, "InGeneralRoom") where S : GWDScript<S> {
    override val failedComponent: TreeComponent<S> = HasKC(script)
    override val successComponent: TreeComponent<S> = GeneralAlive(script)

    override fun validate(): Boolean {
        return script.god.area.contains(me)
    }
}

class GeneralAlive<S>(script: S) : Branch<S>(script, "IsGeneralAlive") where S : GWDScript<S> {
    override val failedComponent: TreeComponent<S> = MageAlive(script)
    override val successComponent: TreeComponent<S> = script.generalAliveBranch()

    override fun validate(): Boolean {
        script.general = script.god.getGeneral()
        return script.general.alive()
    }
}