package org.powbot.krulvis.fighter

import org.powerbot.script.Tile

data class FighterProfile(
    val names: List<String> = listOf("Goblin"),
    val centerLocation: Tile = Tile(3255, 3231, 0),
    val radius: Int = 15,
    val safeSpot: Tile? = null,
    val maxDistance: Int = 15
)