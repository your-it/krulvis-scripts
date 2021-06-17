package org.powbot.krulvis.fighter

import org.powerbot.script.Tile

data class FighterProfile(
    val names: List<String> = listOf("Fever spider"),
    val centerLocation: Tile = Tile(2145, 5091, 0),
    val radius: Int = 10,
    val safeSpot: Tile? = Tile(2145, 5091, 0),
    val maxDistance: Int = 0
)