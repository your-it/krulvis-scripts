package org.powbot.krulvis.cannon

import org.powbot.api.Random
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor

class ShouldRepair(script: Cannon) : Branch<Cannon>(script, "Should repair?") {
    override val failedComponent: TreeComponent<Cannon> = ShouldRefill(script)
    override val successComponent: TreeComponent<Cannon> = SimpleLeaf(script, "Repairing") {
        if (brokenCannon?.interact("Repair") == true) {
            waitFor { brokenCannon() == null }
        }
    }

    var brokenCannon: GameObject? = null

    fun brokenCannon() = Objects.stream().name("Broken cannon").firstOrNull()

    override fun validate(): Boolean {
        brokenCannon = brokenCannon()
        return brokenCannon != null
    }
}

class ShouldRefill(script: Cannon) : Branch<Cannon>(script, "Should refill?") {
    override val failedComponent: TreeComponent<Cannon> = SimpleLeaf(script, "Chilling") {}
    override val successComponent: TreeComponent<Cannon> = SimpleLeaf(script, "Refilling") {
        val cannon = script.cannon()
    }

    var nextBalls = Random.nextInt(0, 15)

    override fun validate(): Boolean {
        return script.ballsLeft <= nextBalls
    }
}