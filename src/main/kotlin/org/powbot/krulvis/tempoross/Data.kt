package org.powbot.krulvis.tempoross

import org.powbot.api.Area
import org.powbot.api.Polygon
import org.powbot.api.Tile
import java.awt.Point

object UI {
    const val LOOTING = "looting"
    const val EQUIPMENT = "equipment"
    const val INVENTORY = "inventory"
    const val COOK_FISH = "cook"
    const val SPECIAL_ATTACK = "special attack"
    const val SOLO_METHOD = "solo"
    const val BUCKETS = "buckets"
    const val IMCANDO_HAMMER = "imcando hammer"
}

object Data {


    val WAVE_TIMER = 9500
    val PARENT_WIDGET = 437

    var DOUBLE_FISH_ID = 10569
    val RAW = 25564
    val COOKED = 25565

    val TETHER_ANIM = 3705
    val FILLING_ANIM = 896
    val WATER_ANIM = 832
    val KILLING_ANIM = 618

    val BOAT_AREA = Area(Tile(3129, 2831), Tile(3135, 2843))

    val HARPOON = 311
    val BARB_TAIL_HARPOON = 10129
    val SPEC_HARPOONS = intArrayOf(21028, 21031, 21033, 23762, 23764, 25059, 25373)
    val WEARABLE_HARPOONS = intArrayOf(BARB_TAIL_HARPOON, *SPEC_HARPOONS)
    val HARPOONS = intArrayOf(HARPOON, *WEARABLE_HARPOONS)

}

enum class Side {
    UNKNOWN,
    NORTH,
    SOUTH;

    var mastLocation = Tile(9618, 2817, 0)
    val oddFishingSpot: Tile get() = Tile(mastLocation.x() - 21, mastLocation.y() - 15, 0)

    val cookLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 5, mastLocation.y() + 22, 0)
        else Tile(mastLocation.x() - 22, mastLocation.y() - 21, 0)

    val northCookSpot: Tile get() = Tile(cookLocation.x() + 1, cookLocation.y() - 3, 0)

    val bossPoolLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 11, mastLocation.y() + 5, 0)
        else Tile(mastLocation.x() - 11, mastLocation.y() - 5, 0)

    val bossWalkLocation: Tile
        get() = if (this == NORTH)
            Tile(bossPoolLocation.x(), bossPoolLocation.y() + 2, 0)
        else
            Tile(bossPoolLocation.x(), bossPoolLocation.y() - 2, 0)

    val totemLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 8, mastLocation.y() + 18, 0)
        else Tile(mastLocation.x() - 15, mastLocation.y() - 16, 0)

    val anchorLocation: Tile
        get() = if (this == NORTH)
            Tile(mastLocation.x() + 8, mastLocation.y() + 9, 0)
        else
            Tile(mastLocation.x() - 8, mastLocation.y() - 9, 0)

    //BOAT, SW = Tile(51, 44, 0) SE = Tile(54, 44, 0),
    // DOCK = Tile(64, 57, 0), EAST = Tile(66, 66, 0), NORTH EAST = Tile(66, 80)
    //NORTH WEST = Tile(48, 80, 0)
    val polygon: Area
        get() = if (this == NORTH) {
            Area(
                Tile(mastLocation.x() - 5, mastLocation.y() - 4)
            )
        } else {
            Area()
        }
}