package org.powbot.krulvis.daeyalt

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.requirements.EquipmentRequirement

@ScriptManifest(
	name = "krul DaeyaltMiner",
	description = "Mines Daeyalt essence",
	author = "Krulvis",
	version = "1.0.0",
	scriptId = "404633af-46ed-4121-82da-510d8a040ff7",
	category = ScriptCategory.Mining
)
@ScriptConfiguration.List([
	ScriptConfiguration("Equipment", "What to wear?", OptionType.EQUIPMENT,
		defaultValue = """{"22400":2,"11920":3,"24676":4,"24678":7,"24680":10}""")
])
class DaeyaltMiner : ATScript() {

	override fun createPainter(): ATPaint<*> {
		return DaeyaltPainter(this)
	}

	val equipment by lazy { EquipmentRequirement.forEquipmentOption(getOption("Equipment")) }
	override val rootComponent: TreeComponent<*> = WearingEquipment(this)


}

val ESSENCE = 24706

class DaeyaltPainter(script: DaeyaltMiner) : ATPaint<DaeyaltMiner>(script) {
	override fun buildPaint(paintBuilder: PaintBuilder): Paint {
		return paintBuilder.trackInventoryItem(ESSENCE).trackSkill(Skill.Mining).build()
	}
}

fun main() {
	DaeyaltMiner().startScript()
}