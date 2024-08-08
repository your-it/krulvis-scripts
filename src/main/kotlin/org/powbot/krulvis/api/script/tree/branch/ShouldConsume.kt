package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Food.Companion.needsFood
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Timer

/**
 * ShouldConsume branch usable throughout all scripts
 * Consumes food and potions as long as they are present in
 */
class ShouldConsume<S : ATScript>(
	script: S,
	override val failedComponent: TreeComponent<S>,
) : Branch<S>(script, "Should consume?") {

	override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Consuming") {
		Game.tab(Game.Tab.INVENTORY)
		val prayPot = potions.firstOrNull { it == Potion.PRAYER || it == Potion.SUPER_RESTORE }
		val canSipPray = prayPot?.needsRestore(0) == true
		val food = if (canSipPray) edibleFood() else food
		val pot = potion
		if (food != null && eatTimer.isFinished()) {
			val foodCount = food.getInventoryCount(false)
			if (!food.eat()) return@SimpleLeaf
			val prayPoints = Prayer.prayerPoints()
			eatTimer.reset()
			nextEatExtra = Random.nextInt(1, 8)
			if (!canSipPray) {
				Condition.wait({ food.getInventoryCount() < foodCount }, 250, 15)
			} else if (prayPot?.drink() == true) {
				script.logger.info("Tick-sipping prayer potion")
				sipTimer.reset()
				if (Condition.wait({ Prayer.prayerPoints() > prayPoints }, 250, 15))
					return@SimpleLeaf
			}
		}


		if (pot != null && sipTimer.isFinished()) {
			if (!pot.drink()) return@SimpleLeaf
			sipTimer.reset()
			potionRestore = Random.nextInt(40, 65)
			Condition.wait({ pot != potion() }, 250, 15)
		}
	}

	var food: Food? = null
	var nextEatExtra = Random.nextInt(1, 8)
	val eatTimer = Timer(1800)
	var potion: Potion? = null
	var potionRestore = Random.nextInt(45, 60)
	val sipTimer = Timer(1200)

	private fun food(): Food? {
		val missingHp = missingHP()
		val needsFood = needsFood()
		return foods.firstOrNull {
			(needsFood || missingHp >= it.healing + nextEatExtra) && it.inInventory()
		}
	}

	private fun edibleFood(): Food? {
		val missingHp = missingHP()
		return foods.firstOrNull { it.healing >= missingHp }
	}

	private fun potion(): Potion? = potions.filter { it.hasWith() }
		.firstOrNull {
			it.needsRestore(potionRestore)
		}

	override fun validate(): Boolean {
		if (!eatTimer.isFinished() && !sipTimer.isFinished()) return false
		food = food()
		potion = potion()
		return (food != null && eatTimer.isFinished()) || (potion != null && sipTimer.isFinished())
	}


	companion object {
		var foods: Array<Food> = Food.values()
		var potions: Array<Potion> = Potion.values()
	}
}