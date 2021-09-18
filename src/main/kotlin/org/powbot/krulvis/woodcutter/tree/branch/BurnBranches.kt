package org.powbot.krulvis.woodcutter.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.krulvis.woodcutter.tree.leaf.Burn
import org.powbot.krulvis.woodcutter.tree.leaf.HandleBank

class ShouldBurn(script: Woodcutter) : Branch<Woodcutter>(script, "Should Burn?") {
    override val failedComponent: TreeComponent<Woodcutter> = ShouldBank(script)
    override val successComponent: TreeComponent<Woodcutter> = Burn(script)

    override fun validate(): Boolean {
        return script.burning || (Inventory.isFull() && script.burn)
    }
}
