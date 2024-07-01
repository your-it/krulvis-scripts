package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.rt4.magic.RunePouch
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Item.Companion.HERB_SACK_OPEN
import org.powbot.krulvis.api.extensions.items.Item.Companion.JUG
import org.powbot.krulvis.api.extensions.items.Item.Companion.PIE_DISH
import org.powbot.krulvis.api.extensions.items.Item.Companion.RUNE_POUCH
import org.powbot.krulvis.api.extensions.items.Item.Companion.SEED_BOX_OPEN
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.script.tree.branch.ShouldHighAlch
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Loot
import kotlin.math.max

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
		script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.equip()
		waitFor { script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.inInventory() != true }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script, ShouldDropTrash(script))

	override fun validate(): Boolean {
		val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
		return ammo != null && ammo.inInventory()
			&& (Inventory.isFull()
			|| (ammo.getInvItem()?.stack ?: -1) > 5
			|| !ammo.inEquipment()
			)
	}
}

class ShouldDropTrash(script: Fighter) : Branch<Fighter>(script, "Should Drop Trash?") {

	val TRASH = intArrayOf(VIAL, PIE_DISH, JUG)
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Dropping vial") {
		if (Inventory.stream().id(*TRASH).firstOrNull()?.interact("Drop") == true)
			waitFor { Inventory.stream().id(*TRASH).firstOrNull() == null }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldInsertRunes(script)

	override fun validate(): Boolean {
		return Inventory.stream().id(*TRASH).firstOrNull() != null
	}
}

class ShouldInsertRunes(script: Fighter) : Branch<Fighter>(script, "Should Insert Runes?") {

	var inventoryRune: Item? = null

	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Inserting runes") {
		if (Inventory.selectedItem().id != inventoryRune?.id) {
			inventoryRune?.interact("Use")
		} else if (Inventory.stream().id(RUNE_POUCH).firstOrNull()?.interact("Use") == true) {
			waitFor { getInsertableRune() == null }
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldBuryBones(script)

	fun getInsertableRune(): Item? {
		val runes = RunePouch.runes()
		return Inventory.stream()
			.firstOrNull { invItem -> runes.any { invItem.id == it.first.id && it.second < 16000 - invItem.stack } }
	}

	override fun validate(): Boolean {
		inventoryRune = getInsertableRune()
		return RunePouch.inventory() && inventoryRune != null
	}
}


///BANKING SITS IN BETWEEN HERE

class CanLoot(script: Fighter) : Branch<Fighter>(script, "Can loot?") {
	override val successComponent: TreeComponent<Fighter> = Loot(script)
	override val failedComponent: TreeComponent<Fighter> = ShouldExitRoom(script)

	private fun makeSpace(worth: Int): Boolean {
		if (worth > 5000) {
			val pot = Inventory.stream().name("(1)").action("Drink").first()
			return pot.interact("Drink")
		} else {
			val edibleFood = Food.getFirstFood()
			return edibleFood?.eat() == true
		}
	}

	override fun validate(): Boolean {
		val loot = script.loot().map { it to it.stackSize() * max(GrandExchange.getItemPrice(it.id()), it.price()) }
		if (loot.isEmpty() || !loot.first().first.reachable()) {
			return false
		}
		val lootString = loot.joinToString { it.first.name() + ": " + it.second }
		script.logger.info("Loot found: [${lootString}]")
		val worth = loot.sumOf { it.second }
		val hasHerbSack = Inventory.containsOneOf(HERB_SACK_OPEN)
		val hasSeedBox = Inventory.containsOneOf(SEED_BOX_OPEN)
		return if (Inventory.isFull()) {
			loot.any { it.first.stacks(hasHerbSack, hasSeedBox) } || makeSpace(worth)
		} else {
			worth >= script.minLootPrice
		}
	}

	private fun GroundItem.stacks(herbSack: Boolean, seedBox: Boolean): Boolean {
		return stackable() && Inventory.containsOneOf(id())
			|| (herbSack && name.contains("grimy", true))
			|| (seedBox && name.contains("seed", true))
	}
}
