package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext
import org.powerbot.script.Tile


open class LocalTileAction(
    override val parent: LocalWebAction,
    destination: Tile,
    finalDestination: Tile
) :
    LocalWebAction(destination, finalDestination) {

    override val type: WebActionType =
        WebActionType.WALKING

    override fun getCost(ctx: ATContext): Double {
        if (destination == finalDestination) {
            return 0.0
        }
        return 1.0
    }

    override fun getNeighbors(ctx: ATContext): MutableList<LocalWebAction> {
        return ctx.lpf.getLocalNeighbors(this, finalDestination)
    }

    override fun execute(ctx: ATContext): Boolean {
        return ctx.walking.step(destination)
    }

}