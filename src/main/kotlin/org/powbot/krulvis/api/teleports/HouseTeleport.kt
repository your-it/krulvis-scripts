package org.powbot.krulvis.api.teleports

import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.utils.Utils.waitFor

interface HouseTeleport : Teleport {

    override fun execute(): Boolean {
        if (!House.isInside()) {
            if (Teleport.houseTeleport?.execute() == true) {
                waitFor(5000) { House.isInside() }
            }
        } else if (House.useRestorePool()) {
            return insideHouseTeleport()
        }
        return false
    }

    fun insideHouseTeleport(): Boolean

}

