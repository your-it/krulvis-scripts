package org.powbot.krulvis.api.extensions.walking

import org.powbot.api.Tile

interface Path {

    fun traverse(): Boolean

    fun finalDestination(): Tile
}