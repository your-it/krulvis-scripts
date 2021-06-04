package org.powbot.krulvis.tithe.tree.tree

import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.tithe.TitheFarmer

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {
    override fun execute() {
        script.getPatchTiles()
        val hasEnoughWater = script.hasEnoughWater()
        val hasSeeds = script.hasSeeds()
        val nextPatch = script.patches.firstOrNull {
            it.needsAction() && ((hasEnoughWater && hasSeeds) || !it.isEmpty())
        } ?: return
        if (nextPatch.isEmpty()) {
            val seed = script.getSeed()
            if (nextPatch.plant(seed)) {
                val doneDidIt = Utils.waitFor(Utils.long()) {
                    nextPatch.isPlanted(true)
                }
                println("Planted on $nextPatch: $doneDidIt")
            }
        } else if (nextPatch.needsWatering()) {
            if (nextPatch.water()) {
                val doneDidIt = Utils.waitFor(Utils.long()) {
                    !nextPatch.needsWatering(true)
                }
                println("Watered on $nextPatch: $doneDidIt")
            }
        } else if (nextPatch.isDone()) {
            if (nextPatch.harvest()) {
                val doneDidIt = Utils.waitFor(Utils.long()) {
                    nextPatch.isEmpty(true)
                }
                println("Harvested on $nextPatch: $doneDidIt")
            }
        } else if (nextPatch.blighted()) {
            if (nextPatch.clear()) {
                val doneDidIt = Utils.waitFor(Utils.long()) {
                    nextPatch.isEmpty(true)
                }
                println("Cleared on $nextPatch: $doneDidIt")
            }
        }
    }
}