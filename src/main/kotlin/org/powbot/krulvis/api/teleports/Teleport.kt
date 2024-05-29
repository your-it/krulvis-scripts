package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.Requirement

interface Teleport {

    val action: String

    val requirements: List<Requirement>

    fun execute(): Boolean

    companion object {

        var houseTeleport: Teleport? = null

        fun forName(name: String): Teleport? {
            return SpellTeleport.forName(name) ?: ItemTeleport.forName(name)
        }
    }
}