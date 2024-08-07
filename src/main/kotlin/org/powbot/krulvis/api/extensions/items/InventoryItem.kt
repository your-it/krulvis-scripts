package org.powbot.krulvis.api.extensions.items

import org.powbot.mobile.rscache.loader.ItemLoader

class InventoryItem(override val id: Int) : Item {
	override val ids: IntArray = intArrayOf(id)
	override val name: String by lazy { ItemLoader.lookup(id)!!.name() }
	override val stackable: Boolean by lazy { ItemLoader.lookup(id)!!.stackable() }

	override fun hasWith(): Boolean = inInventory()

	override fun getCount(countNoted: Boolean): Int {
		return getInventoryCount(countNoted)
	}

}