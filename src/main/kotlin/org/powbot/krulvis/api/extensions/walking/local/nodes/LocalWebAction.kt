package org.powbot.krulvis.api.extensions.walking.local.nodes

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.utils.actions.Action
import org.powerbot.script.Tile
import kotlin.math.abs

fun Tile.distanceM(dest: Tile): Int {
    if (floor() != dest.floor()) {
        return Int.MAX_VALUE
    }
    return abs(dest.x() - x()) + abs(dest.y() - y())
}

enum class WebActionType {
    WALKING, DOOR
}


abstract class LocalWebAction(val destination: Tile, val finalDestination: Tile) : Action {

    abstract val type: WebActionType

    abstract val parent: LocalWebAction

    val heuristics: Double = destination.distanceM(finalDestination).toDouble()

    abstract fun getCost(ctx: ATContext): Double

    abstract fun getNeighbors(ctx: ATContext): MutableList<LocalWebAction>

    fun getPathCost(ctx: ATContext): Double {
        var cost = this.getCost(ctx)
        var curr = this
        while (curr.parent !is StartTile) {
            cost += curr.parent.getCost(ctx)
            curr = curr.parent
        }
        return cost
    }
}

class StartTile(startTile: Tile, finalDestination: Tile) : LocalWebAction(startTile, finalDestination) {
    override val type: WebActionType =
        WebActionType.WALKING

    override val parent: LocalWebAction = this

    override fun getCost(ctx: ATContext): Double = 1.0

    override fun getNeighbors(ctx: ATContext): MutableList<LocalWebAction> {
        return ctx.lpf.getLocalNeighbors(this, finalDestination)
    }

    override fun execute(ctx: ATContext): Boolean {
        return ctx.walking.step(destination)
    }
}