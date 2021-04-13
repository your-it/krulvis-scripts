package org.powbot.krulvis.api.antiban

import org.powbot.krulvis.api.utils.Random

class OddsModifier {

    var minModifier: Double = 0.0
        private set
    var maxModifier: Double = 0.0
        private set

    init {
        reset()
    }

    fun reset() {
        minModifier = Random.nextGaussian(75, 115) / 100.0
        maxModifier = Random.nextGaussian(85, 125) / 100.0
    }

    fun gaussian() = Random.gaussian(minModifier, maxModifier)
}