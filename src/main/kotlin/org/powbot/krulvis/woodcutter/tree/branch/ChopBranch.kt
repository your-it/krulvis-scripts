package org.powbot.krulvis.woodcutter.tree.branch

import org.powbot.api.Condition.sleep
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.Random
import org.powbot.krulvis.woodcutter.Woodcutter
import org.powbot.krulvis.woodcutter.tree.leaf.Chop
import org.powbot.krulvis.woodcutter.tree.leaf.Walk

class AtSpot(script: Woodcutter) : Branch<Woodcutter>(script, "At Spot?") {
    override val failedComponent: TreeComponent<Woodcutter> = Walk(script)
    override val successComponent: TreeComponent<Woodcutter> = IsChopping(script)

    override fun validate(): Boolean {
        return script.trees.any { it.distance() < 5 }
    }
}

class IsChopping(script: Woodcutter) : Branch<Woodcutter>(script, "Is Chopping?") {
    override val failedComponent: TreeComponent<Woodcutter> = Chop(script)
    override val successComponent: TreeComponent<Woodcutter> =
        SimpleLeaf(script, "Chilling") { sleep(Random.nextInt(500, 700)) }

    override fun validate(): Boolean {
        return if (System.currentTimeMillis() - script.lastChopAnim < 1000) {
            true
        } else if (Players.local().animation() != -1) {
            script.lastChopAnim = System.currentTimeMillis()
            true
        } else {
            false
        }
    }
}