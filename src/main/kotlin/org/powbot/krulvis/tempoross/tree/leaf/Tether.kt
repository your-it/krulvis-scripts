package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.getWalkableNeighbors
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class Tether(script: Tempoross) : Leaf<Tempoross>(script, "Tethering") {
    override fun execute() {
        if (script.isTethering()) {
            println("Waiting for wave to pass..")
            if (waitFor {
                    val tetherMast =
                        Objects.stream().filter { it.tile() in listOf(script.mastLocation, script.totemLocation) }
                            .nearest().first()
                    script.waveTimer.isFinished() && !tetherMast.actions().contains("Untether")
                }) {
                println("Done tethering...")
                script.waveTimer.stop()
                sleep(Random.nextInt(1000, 1200))
            }
            return
        }
        val poleO = script.getTetherPole()
        if (!poleO.isPresent) {
            println("Can't find tetherpole")
            return
        }
        val pole = poleO.get()
        val poleTiles = pole.tile().getWalkableNeighbors(true)
        val nearestTile = poleTiles.minByOrNull { it.distance() }
        val safeTile = poleTiles.filterNot { script.blockedTiles.contains(it) }.minByOrNull { it.distance() }
        if (safeTile == nearestTile) {
            if (interact(pole, "Tether")) {
                waitFor(2500) { script.isTethering() }
            }
        } else {
            walk(safeTile)
        }
    }

}