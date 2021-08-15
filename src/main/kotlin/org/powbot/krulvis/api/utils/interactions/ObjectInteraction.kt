package org.powbot.krulvis.api.utils.interactions

import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import java.util.*

open class ObjectInteraction(
    val name: String,
    override val action: String,
    override val tile: Tile
) :
    Interaction<GameObject> {

    constructor(name: String, action: String) : this(name, action, Tile.Nil)

    override fun getEntity(): Optional<GameObject> {
        return if (tile != Tile.Nil) Objects.stream().at(tile).name(name).action(action).findFirst()
        else Objects.stream().name(name).action(action).nearest().findFirst()
    }

}