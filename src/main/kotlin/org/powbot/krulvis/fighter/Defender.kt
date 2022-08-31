package org.powbot.krulvis.fighter

import org.powbot.api.Tile
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory

object Defender {

    val defenders = listOf(8844, 8845, 8846, 8847, 8848, 8849, 8850, 12954)
    var lastDefenderIndex = -1

    private val basementTile = Tile(2915, 9966, 0)
    private val beginnerTile = Tile(2859, 3545, 2)
    fun killSpot() = if (lastDefenderIndex >= 6) basementTile else beginnerTile

    fun currentDefenderIndex(): Int {
        val inv = Inventory.stream().list().map { it.id }
        val equipped = Equipment.itemAt(Equipment.Slot.OFF_HAND).id
        val defender = defenders.lastOrNull { it in inv || equipped == it }
        return defenders.indexOf(defender)
    }

    fun defenderId() = if (lastDefenderIndex >= 0) defenders[lastDefenderIndex] else 0

    fun defender() =
        org.powbot.krulvis.api.extensions.items.Equipment(emptyList(), Equipment.Slot.OFF_HAND, defenderId())

}