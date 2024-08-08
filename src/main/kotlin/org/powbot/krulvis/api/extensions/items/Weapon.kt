package org.powbot.krulvis.api.extensions.items

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment

const val DDS = "DDS"
const val ARCLIGHT = "ARCLIGHT"

enum class Weapon(val specialPercentage: Int, override val ids: IntArray) : IEquipmentItem {
	DDS(25, intArrayOf(1215, 1231, 5680, 5698)),
	ARCLIGHT(50, intArrayOf(19675)),
	;

	override val slot: Equipment.Slot = Equipment.Slot.MAIN_HAND
	override val stackable: Boolean = false

	fun canSpecial(): Boolean = Combat.specialPercentage() >= specialPercentage

}