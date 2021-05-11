package org.powbot.krulvis.api.utils.interactions

import org.powerbot.script.Tile
import org.powerbot.script.rt4.GameObject
import java.util.*

open class ObjectInteraction(
    val name: String,
    override val action: String,
    override val tile: Tile
) :
    Interaction<GameObject> {

    constructor(name: String, action: String) : this(name, action, Tile.NIL)

    override fun getEntity(): Optional<GameObject> {
        return if (tile != Tile.NIL) ctx.objects.toStream().at(tile).name(name).action(action).findFirst()
        else ctx.objects.toStream().name(name).action(action).nearest().findFirst()
    }

}