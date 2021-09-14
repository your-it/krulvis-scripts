package org.powbot.krulvis.combiner.tree.branch

import org.powbot.api.Production
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.combiner.Combiner
import org.powbot.krulvis.combiner.tree.leaf.HandleBank
import org.powbot.krulvis.combiner.tree.leaf.OpenComponent

class ShouldBank(
    script: Combiner
) : Branch<Combiner>(script, "Should bank") {
    override val failedComponent: TreeComponent<Combiner> = StoppedCombining(script)
    override val successComponent: TreeComponent<Combiner> = HandleBank(script)

    override fun validate(): Boolean {
        return script.items.any { !Inventory.containsOneOf(it.first) }
    }
}

class StoppedCombining(
    script: Combiner
) : Branch<Combiner>(script, "Stopped Combining") {
    override val failedComponent: TreeComponent<Combiner> =
        SimpleLeaf(script, "Chilling") { sleep(Random.nextInt(600, 1000)) }
    override val successComponent: TreeComponent<Combiner> = ComponentOpen(script)

    override fun validate(): Boolean {
        return script.stoppedUsing()
    }
}

class ComponentOpen(
    script: Combiner
) : Branch<Combiner>(script, "Combine Component Open") {
    override val failedComponent: TreeComponent<Combiner> = OpenComponent(script)
    override val successComponent: TreeComponent<Combiner> = SimpleLeaf(script, "Combine") {
        val comp = script.combineWidgetActionEvent?.widget() ?: return@SimpleLeaf
        if (comp.interact(script.combineWidgetActionEvent?.interaction, false)) {
            waitFor(long()) { !script.stoppedUsing() }
        }
    }

    override fun validate(): Boolean {
        val combineWidget = script.combineWidgetActionEvent?.widget()
        return combineWidget?.visible() == true
    }
}