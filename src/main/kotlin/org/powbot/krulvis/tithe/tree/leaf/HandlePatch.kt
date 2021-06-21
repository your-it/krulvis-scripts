package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.turnRunOn
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.logging.Logger

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {


    val logger = Logger.getLogger("HandlingPlant")

    override fun execute() {
        if (script.lock) return
        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.getWaterCount() > 0
        val patches = script.patches.sortedWith(compareBy(Patch::id, Patch::index))
        val patch = patches.firstOrNull {
            it.needsAction() && (hasEnoughWater || it.isDone())
        } ?: return
        logger.warning("Handling patch: $patch")
        turnRunOn()
        if (patch.needsWatering()) {
            val waterCount = script.getWaterCount()
            if (patch.walkBetween(script.patches) && patch.water()) {
                val doneDidIt = waitFor(2500) {
                    script.getWaterCount() < waterCount
                }
                logger.info("Watered on $patch: $doneDidIt")
            }
        } else if (patch.isDone()) {
            val harvestCount = ctx.inventory.getCount(*Data.HARVEST)
            if (patch.walkBetween(script.patches) && patch.harvest()) {
                val doneDidIt = waitFor(2500) {
                    ctx.inventory.getCount(*Data.HARVEST) > harvestCount
                }
                logger.info("Harvested on $patch: $doneDidIt")
            }
        } else if (patch.blighted()) {
            if (patch.walkBetween(script.patches) && patch.clear()) {
                val doneDidIt = waitFor(2500) {
                    patch.isEmpty(true)
                }
                logger.info("Cleared on $patch: $doneDidIt")
            }
        }
    }

}