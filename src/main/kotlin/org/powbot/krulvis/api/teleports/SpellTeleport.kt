package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.Requirement
import org.powbot.api.requirement.RunePowerRequirement
import org.powbot.api.rt4.Magic

enum class SpellTeleport(val spell: Magic.MagicSpell, override val action: String = "Cast") : Teleport {
    FALADOR_TELEPORT(Magic.Spell.FALADOR_TELEPORT)
    ;

    override val requirements: List<Requirement> = spell.requirements().filterIsInstance<RunePowerRequirement>()

    override fun execute(): Boolean {
        return spell.cast(action)
    }

    companion object {
        fun forName(name: String): SpellTeleport? {
            return values().firstOrNull { it.name.replace("_", " ").equals(name, true) }
        }
    }
}


const val FALADOR_TELEPORT = "Falador teleport"