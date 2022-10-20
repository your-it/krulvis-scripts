package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.ATContext.getWalkableNeighbors
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Leaf
import org.powbot.api.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class Tether(script: Tempoross) : Leaf<Tempoross>(script, "Tethering") {
    override fun execute() {
        if (script.isTethering()) {
            script.log.info("Waiting for wave to pass..")
            if (waitFor(script.waveTimer.getRemainder().toInt()) {
                    script.waveTimer.isFinished() && !script.isTethering()
                }) {
                script.log.info("Done tethering...")
                script.waveTimer.stop()
                sleep(Random.nextInt(1000, 1200))
            }
            return
        }
        val pole = script.getTetherPole() ?: return
        val poleTiles = pole.tile().getWalkableNeighbors(diagonalTiles = true)
        val nearestTile = poleTiles.minByOrNull { it.distance() }
        val safeTile = poleTiles.filterNot { script.burningTiles.contains(it) }.minByOrNull { it.distance() }
        if (safeTile == nearestTile) {
            if (walkAndInteract(pole, "Tether")) {
                waitFor(2500) { script.isTethering() }
            }
        } else {
            walk(safeTile)
        }
    }

}