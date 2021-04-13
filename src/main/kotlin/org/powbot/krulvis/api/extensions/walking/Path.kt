package org.powbot.krulvis.api.extensions.walking

import org.powbot.krulvis.api.ATContext
import org.powerbot.script.Tile

abstract class Path : ATContext {
    
    abstract fun traverse(): Boolean

    abstract fun finalDestination(): Tile
}