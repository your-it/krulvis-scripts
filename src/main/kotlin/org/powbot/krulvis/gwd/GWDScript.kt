package org.powbot.krulvis.gwd

import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.Npc
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.GWD_GHOMMAL_HILT
import org.powbot.krulvis.api.extensions.teleports.Teleport
import org.powbot.krulvis.api.extensions.teleports.TeleportMethod
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.script.KillerScript
import org.powbot.krulvis.fighter.BANK_TELEPORT_OPTION
import org.powbot.krulvis.fighter.EQUIPMENT_OPTION
import org.powbot.krulvis.fighter.INVENTORY_OPTION
import org.powbot.krulvis.fighter.MONSTER_TELEPORT_OPTION

@ScriptConfiguration.List(
	[
		ScriptConfiguration(EQUIPMENT_OPTION, "What to wear?", OptionType.EQUIPMENT),
		ScriptConfiguration(INVENTORY_OPTION, "What to bring in Inventory?", OptionType.INVENTORY),
		ScriptConfiguration(
			BANK_TELEPORT_OPTION,
			"How to get to bank?",
			OptionType.STRING,
			defaultValue = CASTLE_WARS_JEWELLERY_BOX,
			allowedValues = [CASTLE_WARS_JEWELLERY_BOX]
		),
		ScriptConfiguration(
			MONSTER_TELEPORT_OPTION,
			"How to get to GWD?",
			OptionType.STRING,
			defaultValue = GWD_GHOMMAL_HILT,
			allowedValues = [GWD_GHOMMAL_HILT]
		),
	]
)
abstract class GWDScript<S: GWDScript<S>>(val god: GWD.God) : KillerScript(false) {

	val equipment by lazy { EquipmentRequirement.forEquipmentOption(getOption(EQUIPMENT_OPTION)) }
	val inventory by lazy { InventoryRequirement.forOption(getOption(INVENTORY_OPTION)) }
	val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }
	val gwdTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }

	var general: Npc = Npc.Nil
	var mageBG: Npc = Npc.Nil
	var rangeBG: Npc = Npc.Nil
	var meleeBG: Npc = Npc.Nil

	abstract fun generalAliveBranch(): TreeComponent<S>
	abstract fun generalDeadBranch(): TreeComponent<S>

	override val ammoIds: IntArray by lazy { intArrayOf(0) }

	override fun GroundItem.isLoot(): Boolean {
		val name = name().lowercase()
		return GWD.lootNames.any { it in name }
	}

}