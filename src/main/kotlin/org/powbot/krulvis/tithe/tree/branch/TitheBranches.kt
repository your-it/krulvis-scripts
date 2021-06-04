package org.powbot.krulvis.tithe.tree.branch

import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.SimpleLeaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.TitheFarmer
import org.powbot.krulvis.tithe.tree.tree.Deposit
import org.powbot.krulvis.tithe.tree.tree.HandlePatch
import org.powbot.krulvis.tithe.tree.tree.Refill
import org.powbot.krulvis.tithe.tree.tree.WalkBack

class ShouldRefill(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should refill") {
    override val successComponent: TreeComponent<TitheFarmer> = Refill(script)
    override val failedComponent: TreeComponent<TitheFarmer> = ShouldDeposit(script)

    override fun validate(): Boolean {
        script.refreshPatches()
        if (script.startPoints == -1) {
            script.startPoints = script.getPoints()
        } else {
            script.gainedPoints = script.getPoints() - script.startPoints
        }
        if (script.lastLeaf.name != "Waiting...") {
            script.chillTimer.reset()
        }
        println("Found: ${script.patches.size} patches: nill=${script.patches.count { it.isNill }}")
        return ctx.inventory.toStream().list().none { it.id() in Data.WATER_CANS } ||
                (ctx.inventory.toStream().id(Data.EMPTY_CAN).isNotEmpty() && script.patches.all { it.isEmpty() })
    }
}

class ShouldDeposit(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should deposit") {
    override val successComponent: TreeComponent<TitheFarmer> = Deposit(script)

    override val failedComponent: TreeComponent<TitheFarmer> = ShouldMoveCamera(script)

    override fun validate(): Boolean {
        return script.patches.all { it.isEmpty() } && !script.hasSeeds()
    }
}

class ShouldMoveCamera(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should turn camera") {
    override val successComponent: TreeComponent<TitheFarmer> = SimpleLeaf(script, "Moving camera") {
        ctx.camera.angle(Random.nextInt(255, 290))
        ctx.camera.pitch(Random.nextInt(95, 99))
    }

    override val failedComponent: TreeComponent<TitheFarmer> = ShouldHandlePatch(script)

    override fun validate(): Boolean {
        return ctx.camera.yaw() !in 255..290 || ctx.camera.pitch() < 95
    }
}

class ShouldHandlePatch(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should handle patch") {
    override val successComponent: TreeComponent<TitheFarmer> = HandlePatch(script)

    override val failedComponent: TreeComponent<TitheFarmer> = ShouldWalkBack(script)

    override fun validate(): Boolean {
        val hasEnoughWater = script.hasEnoughWater()
        val hasSeeds = script.hasSeeds()
        return script.patches.any { it.needsAction() && ((hasEnoughWater && hasSeeds) || !it.isEmpty()) }
    }
}

class ShouldWalkBack(script: TitheFarmer) : Branch<TitheFarmer>(script, "Should plant") {
    override val successComponent: TreeComponent<TitheFarmer> = WalkBack(script)
    override val failedComponent: TreeComponent<TitheFarmer> = SimpleLeaf(script, "Waiting...") {}

    override fun validate(): Boolean {
        return script.chillTimer.isFinished()
    }
}





