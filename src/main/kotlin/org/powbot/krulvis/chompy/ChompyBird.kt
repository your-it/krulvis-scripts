package org.powbot.krulvis.chompy

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.NpcAnimationChangedEvent
import org.powbot.api.rt4.Npc
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.chompy.tree.branch.BirdSpawned

@ScriptManifest(name = "krul ChompyBird", description = "Kills chompy birds, doesn't Pluck", version = "1.0.0")
class ChompyBird : ATScript() {
	override fun createPainter(): ATPaint<*> = ChompyBirdPainter(this)

	var placingToads: Boolean = false
	var placementTile = Tile.Nil
	var currentTarget: Npc = Npc.Nil
	var kills = 0

	override val rootComponent: TreeComponent<*> = BirdSpawned(this)

	@Subscribe
	fun onNpcAnimation(nae: NpcAnimationChangedEvent) {
		val npc = nae.npc
		if (npc.name == "Chompy bird" && npc.healthBarVisible() && npc.healthPercent() == 0) {
			kills++
		}
	}
}

fun main() {
	ChompyBird().startScript()
}