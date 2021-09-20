package org.powbot.krulvis.construction.tree.branch

import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.construction.Construction

class ShouldBank(script: Construction) : Branch<Construction>(script, "Should Bank") {
    override val failedComponent: TreeComponent<Construction>
        get() = TODO("Not yet implemented")
    override val successComponent: TreeComponent<Construction>
        get() = TODO("Not yet implemented")

    override fun validate(): Boolean {
        TODO("Not yet implemented")
    }
}