package org.powbot.krulvis.api.utils.interactions

import org.powerbot.script.Tile
import org.powerbot.script.rt4.Interactive
import org.powerbot.script.rt4.Npc
import java.util.*

class NpcInteraction(
    val name: String,
    override val action: String,
    override val tile: Tile
) :
    Interaction<Npc> {

    constructor(name: String, action: String) : this(name, action, Tile.NIL)

    override fun getEntity(): Optional<Npc> =
        ctx.npcs.toStream().name(name).action(action)
            .nearest(if (tile == Tile.NIL) ctx.players.local() else tile)
            .findFirst()

}