package org.powbot.krulvis.api.utils.interactions

import org.powbot.api.Locatable
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import java.util.*

/**
 *
 */
interface Interaction<E : Interactive> {

    /**
     * Return the InteractiveEntity to interact with
     */
    fun getEntity(): Optional<E>

    /**
     * The action to click when interacting with the [Interactive].
     */
    val action: String

    /**
     * The tile to find the Entity at with [GameObject], or near with [Npc]
     */
    val tile: Tile

    /**
     * At what distance should the player walk first
     */
    val walkDistance: Int get() = 12


    /**
     * Execute the Interaction
     */
    fun execute(): Boolean {
        val o = getEntity()
        if (!o.isPresent) {
            debug("Didn't get interaction entity...")
            WebWalking.walkTo(tile, false)
            return false
        }

        val entity = o.get()
        debug("Entity: $entity")
        val t = if (tile == Tile.Nil) (entity as Locatable).tile() else tile
        val destination = Movement.destination() ?: Tile.Nil

        if (!entity.inViewport()
            || (destination != tile && tile.distanceTo(if (destination == Tile.Nil) Players.local() else destination) > walkDistance)
        ) {
            if (tile.matrix()?.onMap() == true) {
                Movement.step(tile)
            } else {
                WebWalking.walkTo(t, false)
            }
        }
        return entity.interact(action)
    }

}