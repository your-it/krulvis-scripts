package org.powbot.krulvis.mole

import org.powbot.api.rt4.Npcs
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.mole.tree.branch.ShouldBank

@ScriptManifest(name = "krul GiantMole", description = "1.0.0", category = ScriptCategory.Combat, priv = true)
class GiantMole : ATScript() {
	override fun createPainter(): ATPaint<*> = GMPainter(this)

	fun findMole() = Npcs.stream().name("Giant Mole").first()
	override val rootComponent: TreeComponent<*> = ShouldBank(this)
}