package org.powbot.krulvis.orbcrafter.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.orbcrafter.OrbCrafter

class ShouldBank(script: OrbCrafter) : Branch<OrbCrafter>(script, "ShouldBank") {
    override val successComponent: TreeComponent<OrbCrafter>
        get() = TODO("Not yet implemented")
    override val failedComponent: TreeComponent<OrbCrafter>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }
}