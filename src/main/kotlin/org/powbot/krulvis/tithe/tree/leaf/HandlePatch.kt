package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.Patch.Companion.refresh
import org.powbot.krulvis.tithe.TitheFarmer

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {

    override fun execute() {
//        if (!waitFor(long()) { !ctx.movement.moving() }) {
//            debug("Not interacting yet because still moving")
//            return
//        }

        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.getWaterCount() > 0
        if (Condition.wait({ script.lastPatch?.needsAction(true) == false }, 100, 20)) {
            debug("Last patch: ${script.lastPatch?.index} = handled!")
            script.lastPatch = null
        }
        val patches = script.patches.refresh().sortedWith(compareBy(Patch::id, Patch::index))
        val patch = patches.firstOrNull {
            it.needsAction() && (hasEnoughWater || it.isDone())
        } ?: script.lastPatch ?: return
        debug("Refreshed and selected next patch! INTERACTING")

        if (patch.handle(script.patches)) {
            val lastTick = script.currentTick
            val nextPatchIndex = script.patches.indexOfFirst { it.tile == patch.tile } + 1
            val nextPatch = if (nextPatchIndex == script.patches.size) null else script.patches[nextPatchIndex]
            debug("Next patch index=$nextPatchIndex, nextPatch at tile=${nextPatch?.tile}")
            if (nextPatch != null) {
                script.nextPatch = nextPatch
            }
        }
    }

}