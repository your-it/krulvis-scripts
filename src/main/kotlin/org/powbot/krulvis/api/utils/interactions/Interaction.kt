package org.powbot.krulvis.api.utils.interactions

import org.powbot.api.InteractableEntity
import org.powbot.api.Locatable
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
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
            WebWalking.walkTo(tile, false)
            return false
        }
        val entity = o.get()

        Utils.walkAndInteract(entity as InteractableEntity, action)
        return false
    }

}