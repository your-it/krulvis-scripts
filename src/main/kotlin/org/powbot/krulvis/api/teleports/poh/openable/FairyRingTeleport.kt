package org.powbot.krulvis.api.teleports.poh.openable

import org.powbot.api.requirement.Requirement
import org.powbot.api.rt4.FairyRing
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.teleports.poh.HouseTeleport
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val IDENTIFYER = "fairy ring"
const val FAIRY_RING_DJR = "DJR $IDENTIFYER (POH)"
const val FAIRY_RING_BLS = "BLS $IDENTIFYER (POH)"

class FairyRingTeleport(val combination: String) : HouseTeleport {

	private fun getRings(): GameObject {
		return Objects.stream().type(GameObject.Type.INTERACTIVE).name("Spiritual Fairy Tree", "Fairy ring").first()
	}

	override fun insideHouseTeleport(): Boolean {
		val rings = getRings()
		val lastDestinationAction = rings.actions().firstOrNull { it.contains(combination, true) }
		if (lastDestinationAction != null) {
			return walkAndInteract(rings, lastDestinationAction)
		}
		val configureAction = rings.actions().first { it.contains("configure", true) }
		if (!FairyRing.opened()) {
			if (walkAndInteract(rings, configureAction)) {
				waitForDistance(rings) { FairyRing.opened() }
			}
		}
		if (FairyRing.opened()) {
			return FairyRing.teleport(combination)
		}

		return false
	}


	override val logger: Logger = LoggerFactory.getLogger(javaClass.simpleName)
	override val action: String = "last-destination ($combination)"
	override val requirements: List<Requirement> = emptyList()

	override fun toString(): String {
		return "FairyRingTeleport($combination)"
	}

	companion object {
		fun forName(name: String): FairyRingTeleport? {
			return if (!name.contains(IDENTIFYER)) null
			else {
				val combination = name.substring(0, 3)
				return FairyRingTeleport(combination.uppercase())
			}
		}
	}

}