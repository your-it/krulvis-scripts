package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.closeOpenHUD
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.turnRunOn
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.short
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Patch
import org.powbot.krulvis.tithe.Patch.Companion.refresh
import org.powbot.krulvis.tithe.TitheFarmer
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Game
import org.powerbot.script.rt4.Menu
import java.util.logging.Logger

class HandlePatch(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Handling patch") {


    val logger = Logger.getLogger("HandlingPlant")

    override fun execute() {
        script.getPatchTiles()
        closeOpenHUD()
        val hasEnoughWater = script.getWaterCount() > 0
        val patches = script.patches.sortedWith(compareBy(Patch::id, Patch::index))
        logger.info("Patches sorted is: ${patches.joinToString()}")
        val patch = patches.firstOrNull {
            it.needsAction() && (hasEnoughWater || it.isDone())
        } ?: return
        logger.info("Handling patch: $patch")
        turnRunOn()
        if (patch.needsWatering()) {
            val waterCount = script.getWaterCount()
            if (patch.walkBetween(script.patches) && patch.water() && waitFor(2500) { patch.go.distance() < 3 }) {
                prepareNextInteraction(patches)
                val doneDidIt = waitFor(2500) {
                    script.getWaterCount() < waterCount
                }
                logger.info("Watered on $patch: $doneDidIt")
            }
        } else if (patch.isDone()) {
            if (patch.walkBetween(script.patches) && patch.harvest() && waitFor(2500) { patch.go.distance() < 3 }) {
                prepareNextInteraction(patches)
                val doneDidIt = waitFor(2500) {
                    patch.isEmpty(true)
                }
                logger.info("Harvested on $patch: $doneDidIt")
            }
        } else if (patch.blighted()) {
            if (patch.walkBetween(script.patches) && patch.clear() && waitFor(2500) { patch.go.distance() < 3 }) {
                prepareNextInteraction(patches)
                val doneDidIt = waitFor(2500) {
                    patch.isEmpty(true)
                }
                logger.info("Cleared on $patch: $doneDidIt")
            }
        }
    }

    fun prepareNextInteraction(patches: List<Patch>) {
        val facingTile = me.facingTile()
        if (facingTile == Tile.NIL) return
        val current = patches.minByOrNull { it.tile.distanceTo(facingTile) } ?: return
        val nextPatch = patches.minusElement(current)
            .refresh()
            .sortedWith(compareBy(Patch::id, Patch::index))
            .firstOrNull { it.needsAction() }
        if (nextPatch != null) {
            println("Preparing next patch interaction...")
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
                ATContext.clickMenu(Menu.filter("Cancel"))
            }
        }
    }
}