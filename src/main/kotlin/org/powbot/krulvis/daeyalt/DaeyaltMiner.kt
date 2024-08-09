package org.powbot.krulvis.daeyalt

import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint

@ScriptManifest(
	name = "krul DaeyaltMiner",
	description = "Mines Daeyalt essence",
	author = "Krulvis",
	version = "1.0.0"
)
class DaeyaltMiner : ATScript() {

	override fun createPainter(): ATPaint<*> {
		return DaeyaltPainter(this)
	}

	override val rootComponent: TreeComponent<*> = ShouldSpecial(this)


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