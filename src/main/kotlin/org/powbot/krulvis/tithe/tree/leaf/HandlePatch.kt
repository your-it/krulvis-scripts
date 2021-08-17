package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.logging.Logger

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {

    override fun execute() {
        if (script.lock) {
            script.log.warning("Handle patch locked")
            return
        }
//        if (!waitFor(long()) { !ctx.movement.moving() }) {
//            debug("Not interacting yet because still moving")
//            return
//        }

        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.getWaterCount() > 0
        val patches = script.patches.sortedWith(compareBy(Patch::id, Patch::index))
        val patch = patches.firstOrNull {
            it.needsAction() && (hasEnoughWater || it.isDone())
        } ?: return
        script.log.warning("Handling patch: $patch")
        if (patch.handle(script.patches)) {
            sleep(1000)
            script.log.warning("Handling patch interaction was successful")
        }
    }

}