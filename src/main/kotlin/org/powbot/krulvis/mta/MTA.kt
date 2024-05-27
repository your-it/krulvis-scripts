package org.powbot.krulvis.mta

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.SimpleBranch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.mta.rooms.*
import org.powbot.krulvis.mta.tree.leafs.GoInside

@ScriptManifest(
	name = "krul MagicTrainingArena",
	description = "Does MTA for points",
	version = "1.0.0",
	category = ScriptCategory.Magic
)
@ScriptConfiguration.List(
	[
		ScriptConfiguration(
			name = "Method",
			description = "Which room to clear? (stand in room before starting script)",
			allowedValues = arrayOf(ALCHEMY_METHOD, ENCHANTING_METHOD, GRAVEYARD_METHOD, TELEKINETIC_METHOD),
			defaultValue = TELEKINETIC_METHOD
		),
	]
)
class MTA : ATScript() {

	val method by lazy { getOption<String>("Method") }

	lateinit var room: MTARoom

	var gainedPoints = 0
	var startCash = 0
	var gainedAlchemyPoints = 0


	override fun createPainter(): ATPaint<*> = MTAPainter(this)

	private var methodBranch: TreeComponent<MTA> = SimpleLeaf(this, "Nil") {}

	override val rootComponent: TreeComponent<*> = SimpleBranch(this, "Inside room",
		successComponent = methodBranch,
		failedComponent = GoInside(this)
	) {
		room.inside()
	}


	override fun onStart() {
		super.onStart()
		room = rooms[method]!!
		TelekineticRoom.resetRoom()
		methodBranch = room.rootComponent(this)
	}

	@Subscribe
	fun onMessageEvent(e: MessageEvent) {
		val txt = e.message
		if (txt == "The cupboard is empty.") {
			AlchemyRoom.EMPTY_CUPBOARD = AlchemyRoom.getCupboard().id
		}
	}

	@Subscribe
	fun onInventoryEvent(e: InventoryChangeEvent) {
		if (e.itemId == 995 && e.quantityChange > 0) {

		}
	}

}

fun main() {
	MTA().startScript("127.0.0.1", "GIM", true)
}