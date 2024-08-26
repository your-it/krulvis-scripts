package org.powbot.krulvis.api.extensions.items.container

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.MessageEvent
import org.powbot.krulvis.api.extensions.items.Item


enum class Emptiable(override val CLOSED_ID: Int, override val OPEN_ID: Int) : Item, Openable {
	HERB_SACK(13226, 24478),
	COAL_BAG(12019, 24480),
	GEM_BAG(12020, 24481),
	SEED_BOX(13639, 24482),
	;

	override val itemName: String = name.lowercase().replace("_", " ")
	override val stackable: Boolean = false
	override val ids: IntArray = intArrayOf(CLOSED_ID, OPEN_ID)

	var emptied = false
	override fun hasWith(): Boolean = getInventoryCount() > 0

	override fun getCount(countNoted: Boolean): Int = getInventoryCount()

	fun empty(): Boolean {
		if (emptied) return true
		val invItem = getInvItem() ?: return true
		if (invItem.interact("Empty")) {
			emptied = true
			return true
		}
		return false
	}


	@Subscribe
	fun messageEvent(evt: MessageEvent) {
		if (evt.message == "The $itemName is now empty.") {
			emptied = true
		}
	}


	companion object {
		fun requireEmpty() {
			values().forEach { it.emptied = false }
		}

		fun emptyAll() = values().all { it.empty() }

		fun openAll() = values().all { it.open() }
	}
}