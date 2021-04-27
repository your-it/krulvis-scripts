package org.powbot.krulvis.api.utils.interactions

import org.powbot.krulvis.walking.PBWebWalkingService
import org.powerbot.script.ClientContext
import org.powerbot.script.Locatable
import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import org.powerbot.script.rt4.Interactive
import org.powerbot.script.rt4.Npc
import java.util.*

/**
 *
 */
interface Interaction<E : Interactive> {

    val ctx: org.powerbot.script.rt4.ClientContext get() = ClientContext.ctx()

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
        if (o.isEmpty) {
            println("Didn't get interaction entity...")
            PBWebWalkingService.walkTo(tile, false)
            return false
        }

        val entity = o.get()
        println("Entity: $entity")
        val t = if (tile == Tile.NIL) (entity as Locatable).tile() else tile
        val destination = ctx.movement.destination() ?: Tile.NIL

        if (!entity.inViewport()
            || (destination != tile && tile.distanceTo(if (destination == Tile.NIL) ctx.players.local() else destination) > walkDistance)
        ) {
            if (tile.matrix(ctx).onMap()) {
                ctx.movement.step(tile)
            } else {
                PBWebWalkingService.walkTo(t, false)
            }
        }
        return entity.interact(action)
    }

}