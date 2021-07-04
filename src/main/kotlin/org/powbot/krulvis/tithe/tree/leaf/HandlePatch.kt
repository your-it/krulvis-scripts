package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.turnRunOn
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.mid
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.logging.Logger

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {


    val logger = Logger.getLogger("HandlingPlant")

    override fun execute() {
        if (script.lock) {
            logger.warning("Handle patch locked")
            return
        }

        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.getWaterCount() > 0
        val patches = script.patches.sortedWith(compareBy(Patch::id, Patch::index))
        val patch = patches.firstOrNull {
            it.needsAction() && (hasEnoughWater || it.isDone())
        } ?: return
        logger.warning("Handling patch: $patch")
        if (patch.handle(script.patches)) {
            sleep(1000)
            logger.warning("Handling patch interaction was successful")
        }
    }

}