package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.TitheFarmer

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
            if (patch.walkBetween(script.patches) && patch.plant(seed)) {
                val doneDidIt = waitFor(5000) {
                    !patch.isEmpty(true)
                }
                script.log.info("Planted on $patch: $doneDidIt")
                if (doneDidIt) {
                    val tick = script.lastTick
                    val start = System.currentTimeMillis()

                    if (waitFor { script.lastTick > tick || System.currentTimeMillis() > start + 600 }
                        && patch.water()) {
                        if (patch.index < script.patchCount - 1) {
                            Inventory.stream().id(seed).findFirst().ifPresent { it.interact("Use") }
                        }
                        val watered = waitFor(4000) { !patch.needsAction(true) }
                        script.log.info("Watered as well.. $patch: $watered")
                    }
                }
            }
        }
    }

}