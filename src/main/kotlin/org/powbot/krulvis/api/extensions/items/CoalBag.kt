package org.powbot.krulvis.api.extensions.items

import org.powbot.api.event.MessageEvent

object CoalBag : Item {

	val CLOSED = 12019
	val OPEN = 24480

	override val ids: IntArray = intArrayOf(CLOSED, OPEN)
	override val name: String = "Coal bag"
	override val stackable: Boolean = false

	var shouldEmpty = true

	override fun hasWith(): Boolean {
		return inInventory()
	}

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(countNoted)
	}

	@com.google.common.eventbus.Subscribe
	fun messageEvent(evt: MessageEvent) {
		if (evt.message == "The coal bag is now empty.") {
			shouldEmpty = false
		}
	}
}