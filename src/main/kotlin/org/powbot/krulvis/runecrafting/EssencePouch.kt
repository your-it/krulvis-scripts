package org.powbot.krulvis.runecrafting

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.utils.Utils.waitFor

enum class EssencePouch(val capacity: Int, val perfectId: Int) : Item {
	SMALL(3, 5509),
	MEDIUM(6, 5510),
	LARGE(9, 5512),
	GIANT(12, 5514),
	COLOSSAL(40, 26784),
	;

	override val ids: IntArray = intArrayOf(perfectId)
	override val stackable: Boolean = false

	override fun hasWith(): Boolean = getInvItem() != null

	override fun getCount(countNoted: Boolean): Int = if (getInvItem() != null) 1 else 0

	override fun getInvItem(worse: Boolean): org.powbot.api.rt4.Item? = Inventory.stream().nameContains(name.lowercase() + " pouch").firstOrNull()

	private var essenceCount: Int = 0
		set(value) {
			value.coerceIn(0, capacity)
			field = value
		}

	fun getEssenceCount() = essenceCount

	fun filled(): Boolean = essenceCount >= capacity
	fun fill(): Boolean {
		val pouch = getInvItem() ?: return true
		val invEssence = essenceCount()
		if (!pouch.valid()) return false
		if (pouch.interact("Fill")) {
			waitFor(1000) { invEssence != essenceCount() }
			essenceCount += invEssence - essenceCount()
		} else {
			essenceCount = capacity
		}
		return filled()
	}

	fun empty(): Boolean {
		if (Inventory.isFull()) return false
		val pouch = getInvItem() ?: return false
		val invEssence = essenceCount()
		if (pouch.interact("Empty")) {
			waitFor { essenceCount() > invEssence }
			val essenceCount = essenceCount()
			if (essenceCount == 0) this.essenceCount = 0
			else this.essenceCount -= essenceCount - invEssence
			return invEssence < essenceCount()
		}
		return false
	}

	fun shouldRepair(): Boolean {
		val invItem = getInvItem() ?: return false
		return invItem.id != perfectId
	}

	companion object {
		fun essenceCount() = Inventory.stream().nameContains("essence").count().toInt()

		fun inInventory() = values().filter { it.getInvItem() != null }
		fun inBank() = values().filter { it.inBank() }
	}
}