package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.logging.Logger

class Plant(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Planting") {

    override fun execute() {
        val hasEnoughWater = script.hasEnoughWater()
        val hasSeeds = script.hasSeeds()
        val patch = script.patches.firstOrNull { it.isEmpty() }
        if (!hasEnoughWater || !hasSeeds || patch == null) {
            script.planting = false
        } else {
            script.planting = true
            val seed = script.getSeed()
            if (patch.walkBetween("None", script.patches) && patch.plant(seed)) {
                if (patch.index < script.profile.patchCount - 1) {
                    Inventory.stream().id(seed).findFirst().ifPresent { it.interact("Use") }
                }
                val doneDidIt = waitFor(2500) {
                    !patch.isEmpty(true)
                }
                logger.info("Planted on $patch: $doneDidIt")
            }
        }
    }


}