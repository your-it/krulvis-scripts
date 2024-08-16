package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.InteractableEntity
import org.powbot.api.Notifications
import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.EssencePouch
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.leafs.HandleBank
import org.powbot.krulvis.runecrafting.tree.leafs.HandleHouse
import org.powbot.krulvis.runecrafting.tree.leafs.MoveToBank
import org.powbot.mobile.script.ScriptManager

class ShouldBank(script: Runecrafter) : Branch<Runecrafter>(script, "Should bank?") {
	override val failedComponent: TreeComponent<Runecrafter> = AtAltar(script)
	override val successComponent: TreeComponent<Runecrafter> = ShouldOpenBank(script)

	override fun validate(): Boolean {
		if (script.getBank().valid() && !Inventory.isFull()) {
			script.logger.info("At bank without full inventory")
			return true
		}
		return Bank.opened()
			|| script.equipment.any { !it.meets() }
			|| (EssencePouch.inInventory().all { it.getEssenceCount() <= 0 } && EssencePouch.essenceCount() == 0)
	}
}

class ShouldOpenBank(script: Runecrafter) : Branch<Runecrafter>(script, "Should open bank?") {
	override val failedComponent: TreeComponent<Runecrafter> = ShouldEat(script)
	override val successComponent: TreeComponent<Runecrafter> = AtBank(script)

	override fun validate(): Boolean {
		return !Bank.opened()
	}
}

class ShouldEat(script: Runecrafter) : Branch<Runecrafter>(script, "Should eat food?") {
	override val failedComponent: TreeComponent<Runecrafter> = ShouldDrinkStamina(script)
	override val successComponent: TreeComponent<Runecrafter> = HasFood(script)

	override fun validate(): Boolean {
		val hp = currentHP()
		return hp <= script.eatAt || script.food.inInventory()
	}
}

class HasFood(script: Runecrafter) : Branch<Runecrafter>(script, "Has food?") {
	override val failedComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Getting food") {
		if (!script.food.inBank()) {
			Notifications.showNotification("Out of food, stopping script")
			ScriptManager.stop()
		}
		script.food.withdrawExact(script.food.requiredAmount())
	}
	override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Eating") {
		script.food.eat()
		sleep(1200)
	}

	override fun validate(): Boolean {
		return script.food.inInventory()
	}
}

class ShouldDrinkStamina(script: Runecrafter) : Branch<Runecrafter>(script, "Should drink stamina?") {
	override val failedComponent: TreeComponent<Runecrafter> = HandleBank(script)
	override val successComponent: TreeComponent<Runecrafter> = HasStamina(script)

	override fun validate(): Boolean {
		if (Potion.isHighOnStamina() || script.vileVigour) return false
		return Potion.STAMINA.hasWith() || (Movement.energyLevel() < 60 && Potion.STAMINA.inBank())
	}
}

class HasStamina(script: Runecrafter) : Branch<Runecrafter>(script, "Has stamina?") {
	override val failedComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Getting stamina") {
		Potion.STAMINA.withdrawExact(1, worse = true, wait = true)
	}
	override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Drinking stamina") {
		if (Potion.STAMINA.drink()) {
			waitFor { Potion.isHighOnStamina() }
		}
	}

	override fun validate(): Boolean {
		return Potion.STAMINA.inInventory()
	}
}

class AtBank(script: Runecrafter) : Branch<Runecrafter>(script, "At Bank?") {
	override val failedComponent: TreeComponent<Runecrafter> = InHouse(script)
	override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Opening Bank") {
		if (walkAndInteract(bankObj, "Bank")) {
			waitForDistance(bankObj) { Bank.opened() }
		}
	}

	var bankObj: InteractableEntity = GameObject.Nil
	override fun validate(): Boolean {
		bankObj = script.getBank()
		return bankObj.distance() < 20
	}
}

class InHouse(script: Runecrafter) : Branch<Runecrafter>(script, "In House?") {
	override val failedComponent: TreeComponent<Runecrafter> = MoveToBank(script)
	override val successComponent: TreeComponent<Runecrafter> = HandleHouse(script)

	override fun validate(): Boolean {
		return House.isInside()
	}
}