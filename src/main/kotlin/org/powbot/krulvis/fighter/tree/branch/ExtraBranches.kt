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
import org.powbot.krulvis.fighter.tree.leaf.PrayAtAltar
import kotlin.math.max
import kotlin.random.Random

class ShouldEquipAmmo(script: Fighter) : Branch<Fighter>(script, "Should equip ammo?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip ammo") {
		script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.equip()
		waitFor { script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }?.inInventory() != true }
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldHighAlch(script, ShouldEquipGloves(script))

	override fun validate(): Boolean {
		val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
		return ammo != null && ammo.inInventory()
			&& (Inventory.isFull()
			|| (ammo.getInvItem()?.stack ?: -1) > 5
			|| !ammo.inEquipment()
			)
	}
}

class ShouldEquipGloves(script: Fighter) : Branch<Fighter>(script, "Should equip gloves?") {
	override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Equip gloves") {
		val gloves = script.equipment.first { it.slot == Equipment.Slot.HANDS }
		if (gloves.equip()) {
			waitFor { gloves.inEquipment() }
		}
	}
	override val failedComponent: TreeComponent<Fighter> = ShouldDropTrash(script)

	override fun validate(): Boolean {
		val gloves = script.equipment.firstOrNull { it.slot == Equipment.Slot.HANDS } ?: return false
		if (gloves.inEquipment() || !gloves.inInventory()) return false
		return !script.currentTarget.valid() || (script.currentTarget.healthBarVisible() && script.currentTarget.healthPercent() > 10)
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
	override val failedComponent: TreeComponent<Fighter> = ShouldPrayAtAltar(script)

	private fun makeSpace(worth: Int): Boolean {
		val edibleFood = Food.getFirstFood()
		if (edibleFood != null) {
			return edibleFood.eat()
		} else if (worth > 5000) {
			val pot = Inventory.stream().name("(1)").action("Drink").first()
			return pot.interact("Drink")
		}
		return false
	}

	override fun validate(): Boolean {
		val loot = script.loot().map { it to it.stackSize() * max(GrandExchange.getItemPrice(it.id()), it.price()) }
		if (loot.isEmpty() || !loot.first().first.reachable()) {
			return false
		}
		val lootString = loot.joinToString { it.first.name() + ": " + it.second }
		script.logger.info("Loot found: [${lootString}]")
		if (!Inventory.isFull()) return true

		val worth = loot.sumOf { it.second }
		val hasHerbSack = Inventory.containsOneOf(HERB_SACK_OPEN)
		val hasSeedBox = Inventory.containsOneOf(SEED_BOX_OPEN)
		return loot.any { it.first.stacks(hasHerbSack, hasSeedBox) } || makeSpace(worth)
	}

	private fun GroundItem.stacks(herbSack: Boolean, seedBox: Boolean): Boolean {
		return stackable() && Inventory.containsOneOf(id())
			|| (herbSack && name.contains("grimy", true))
			|| (seedBox && name.contains("seed", true))
	}
}


class ShouldPrayAtAltar(script: Fighter) : Branch<Fighter>(script, "ShouldPrayAtAltar?") {
	override val successComponent: TreeComponent<Fighter> = PrayAtAltar(script)
	override val failedComponent: TreeComponent<Fighter> = GettingDefenders(script)


	override fun validate(): Boolean {
		if (!script.prayAtNearbyAltar || Prayer.prayerPoints() >= script.nextPrayRestore) return false
		return Objects.stream().type(GameObject.Type.INTERACTIVE).action("Pray-at").nearest().first().valid()
	}

}
