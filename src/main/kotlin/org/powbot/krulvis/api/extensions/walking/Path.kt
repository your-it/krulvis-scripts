package org.powbot.krulvis.api.extensions.walking

import org.powerbot.script.Tile

abstract class Path {

    abstract fun traverse(): Boolean

    abstract fun finalDestination(): Tile
}