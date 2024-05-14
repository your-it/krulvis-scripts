package org.powbot.krulvis.runecrafting.tree

import org.powbot.api.Area
import org.powbot.api.Point
import org.powbot.api.Polygon
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.me

object Abyss {

    fun inInnerCircle() = innerPolygon.contains(me.tile())

    fun inOuterCircle() = outerArea.contains(me.tile())

    fun getPortal(rune: String): GameObject = Objects.stream(50).type(GameObject.Type.WALL_DECORATION).name("$rune rift").action("Exit-through").first()

    val outerArea = Area(Tile(3004, 4863), Tile(3073, 4800))

    val innerPolygon = Polygon(
            listOf(
                    Point(3021, 4833),
                    Point(3025, 4818),
                    Point(3038, 4814),
                    Point(3051, 4817),
                    Point(3057, 4830),
                    Point(3053, 4844),
                    Point(3039, 4850),
                    Point(3026, 4844)
            )
    )

    fun Polygon.contains(tile: Tile): Boolean {
        return contains(tile.x, tile.y)
    }
}