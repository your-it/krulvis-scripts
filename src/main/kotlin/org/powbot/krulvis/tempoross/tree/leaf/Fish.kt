package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Tempoross
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Npc
import java.util.*

class Fish(script: Tempoross) : Leaf<Tempoross>(script, "Fishing") {

    override fun loop() {
        val fishSpot = script.bestFishSpot
        if (fishSpot.isEmpty) {
            debug("No safe fishing spot found!")
            if (script.blockedTiles.contains(me.tile())) {
                val safeTile = findSaveTile(me.tile())
                debug("We are standing on a dangerous tile! Walking to $safeTile")
                if (safeTile != null && walking.step(safeTile)) {
                    waitFor { me.tile() == safeTile }
                }
            } else if (script.fishSpots.any { it.second.actions.last().destination.distance() <= 1 }) {
                debug("Nearby blocked fishing spot found that is blocked")
                val blockedTile =
                    script.fishSpots.first { it.second.actions.last().destination.distance() <= 1 }.second.actions.last()
                val fireOptional =
                    objects.toStream().name("Fire").within(blockedTile.destination, 2.0).nearest().findFirst()
                if (fireOptional.isPresent) {
                    debug("Dousing nearby fire...")
                    val fire = fireOptional.get()
                    if (interact(fire, "Douse")) {
                        waitFor { objects.toStream().at(fire.tile()).name("Fire").isEmpty() }
                    }
                }
            } else {
                println("No fishing spot found, walking to Totem pole / anchor")
                var path = lpf.findPath(script.totemLocation)
                if (path.isEmpty()) {
                    path = lpf.findPath(script.anchorLocation)
                }
                script.walkWhileDousing(path, false)
            }
            return
        }
        val currentSpot = me.interacting() as Npc
        if (currentSpot.name() == "Fishing spot") {
            if (script.blockedTiles.contains(me.tile())
                || (currentSpot.id() != DOUBLE_FISH_ID && fishSpot.get().id() == DOUBLE_FISH_ID)
            ) {
                println("Moving to double/save fish spot!")
                fishAtSpot(fishSpot.get())
            } else {
                val tetherPole = script.getTetherPole()
                if (tetherPole.isPresent && tetherPole.get().inViewport()) {
                    if (script.oddFishingSpot.distance() <= 1) {
                        println("Fishing at weird spot so using unique camera rotation")
                        camera.pitch(Random.nextInt(1200, 1300))
                    } else {
                        camera.turnTo(tetherPole.get())
                    }
                }
            }
        } else {
            println("Fishing at first spot")
            fishAtSpot(fishSpot.get())
        }
    }

    fun findSaveTile(tile: Tile): Tile? {
        return tile.getWalkableNeighbor(excludeSelf = true, diagonalTiles = true) {
            !script.blockedTiles.contains(it)
        }
    }

    fun fishAtSpot(spot: Npc) {
        if (interact(spot, "Harpoon")) {
            waitFor { me.animation() != -1 && me.interacting().name() == "Fishing spot" }
        } else if (walking.moving()) {
            waitFor { spot.distance() <= 2 }
        }
    }

}