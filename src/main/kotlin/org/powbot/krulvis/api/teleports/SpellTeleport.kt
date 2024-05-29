package org.powbot.krulvis.api.teleports

import org.powbot.api.requirement.Requirement
import org.powbot.api.requirement.RunePowerRequirement
import org.powbot.api.rt4.Magic

const val FALADOR_TELEPORT = "Falador teleport"
const val HOUSE_TELEPORT = "House teleport"

enum class SpellTeleport(val spell: Magic.MagicSpell, override val action: String = "Cast") : Teleport {
	FALADOR_TELEPORT(Magic.Spell.FALADOR_TELEPORT),
	HOUSE_TELEPORT(Magic.Spell.TELEPORT_TO_HOUSE)
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


