package org.powbot.krulvis.tithe.tree.tree

import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.short
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.TitheFarmer
import org.powerbot.script.rt4.Game
import java.util.logging.Logger

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {


    val logger = Logger.getLogger("HandlingPlant")

    override fun execute() {
        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.hasEnoughWater()
        val patches = script.patches.sortedWith(compareBy(Patch::id, Patch::index))
        logger.info("Patches sorted is: ${patches.joinToString()}")
        val patch = patches.firstOrNull {
            it.needsAction() && (hasEnoughWater || it.isDone())
        } ?: return
        logger.info("Handling patch: $patch")
        if (patch.needsWatering()) {
            val waterCount = script.getWaterCount()
            if (patch.walkBetween(script.patches) && patch.water()) {
                waitFor { patch.go.distance() <= 3 }
                prepareNextInteraction(patch)
                val doneDidIt = waitFor(Utils.long()) {
                    script.getWaterCount() < waterCount
                }
                logger.info("Watered on $patch: $doneDidIt")
            }
        } else if (patch.isDone()) {
            if (patch.walkBetween(script.patches) && patch.harvest()) {
                waitFor { patch.go.distance() <= 3 }
                prepareNextInteraction(patch)
                val doneDidIt = waitFor(Utils.long()) {
                    patch.isEmpty(true)
                }
                logger.info("Harvested on $patch: $doneDidIt")
            }
        } else if (patch.blighted()) {
            if (patch.walkBetween(script.patches) && patch.clear()) {
                waitFor { patch.go.distance() <= 3 }
                prepareNextInteraction(patch)
                val doneDidIt = waitFor(Utils.long()) {
                    patch.isEmpty(true)
                }
                logger.info("Cleared on $patch: $doneDidIt")
            }
        }
    }

    fun prepareNextInteraction(patch: Patch) {
        val index = script.patches.indexOf(patch)
        val nextPatch =
            if (index == script.patches.size - 1) null else script.patches[script.patches.indexOf(patch) + 1]
        if (nextPatch?.needsAction(true) == true) {
            if (ctx.client().isMobile) {
                nextPatch.go.click()
            } else {
                nextPatch.go.click(false)
            }
            if (waitFor(short()) { ctx.menu.opened() } && !ctx.menu.contains {
                    it.action in listOf(
                        "Harvest",
                        "Water"
                    )
                }) {
                ctx.menu.close()
            }
        }
    }
}