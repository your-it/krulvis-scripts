package org.powbot.krulvis.api.utils.interactions

import org.powbot.api.Tile
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.rt4.Players
import java.util.*

class NpcInteraction(
    val name: String,
    override val action: String,
    override val tile: Tile
) :
    Interaction<Npc> {

    constructor(name: String, action: String) : this(name, action, Tile.Nil)

    override fun getEntity(): Optional<Npc> =
        Npcs.stream().name(name).action(action)
            .nearest(if (tile == Tile.Nil) Players.local() else tile)
            .findFirst()

}