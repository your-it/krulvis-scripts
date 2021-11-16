package org.powbot.krulvis.combiner.tree.branch

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.api.Random
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner
import org.powbot.krulvis.combiner.tree.leaf.HandleBank
import org.powbot.krulvis.combiner.tree.leaf.Combine

class ShouldBank(
    script: Combiner
) : Branch<Combiner>(script, "Should bank") {
    override val failedComponent: TreeComponent<Combiner> = StoppedCombining(script)
    override val successComponent: TreeComponent<Combiner> = HandleBank(script)

    override fun validate(): Boolean {
        return script.items.any { !Inventory.containsOneOf(it.key) }
    }
}

class StoppedCombining(
    script: Combiner
) : Branch<Combiner>(script, "Stopped Combining") {
    override val failedComponent: TreeComponent<Combiner> =
        SimpleLeaf(script, "Chilling") { sleep(Random.nextInt(600, 1000)) }
    override val successComponent: TreeComponent<Combiner> = ShouldCloseBank(script)

    override fun validate(): Boolean {
        return script.stoppedUsing()
    }
}

class ShouldCloseBank(
    script: Combiner
) : Branch<Combiner>(script, "ShouldCloseBank") {
    override val failedComponent: TreeComponent<Combiner> = Combine(script)
    override val successComponent: TreeComponent<Combiner> = SimpleLeaf(script, "Closing Bank") { Bank.close() }

    override fun validate(): Boolean {
        return Bank.opened()
    }
}