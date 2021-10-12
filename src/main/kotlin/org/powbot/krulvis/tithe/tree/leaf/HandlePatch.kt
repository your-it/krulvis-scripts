package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.logging.Logger

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {

    override fun execute() {
//        if (!waitFor(long()) { !ctx.movement.moving() }) {
//            debug("Not interacting yet because still moving")
//            return
//        }

        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.getWaterCount() > 0
        val patches = script.patches.sortedWith(compareBy(Patch::id, Patch::index))
        if (script.lastPatch?.needsAction(true) == false)
            script.lastPatch = null
        val patch = patches.filterNot { it.tile == script.lastPatch?.tile }
            .firstOrNull {
                it.needsAction() && (hasEnoughWater || it.isDone())
            } ?: script.lastPatch ?: return
        if (patch.handle(script.patches) && waitFor(5000) { !patch.needsAction(true) }) {
            val nextPatchIndex = script.patches.indexOfFirst { it.tile == patch.tile } + 1
            val nextPatch = if (nextPatchIndex == script.patches.size) null
            else script.patches[nextPatchIndex]
            script.log.info("Current patcht index=$nextPatchIndex, nextPatch at tile=${nextPatch?.tile}")
            if (nextPatch != null) {
                nextPatch.walkBetween(patches)
                waitFor(3000) { nextPatch.needsAction(true) }
            }
        }
    }

}