package org.powbot.krulvis.mixology.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mixology.Data
import org.powbot.krulvis.mixology.Data.Ingredient.Companion.paired
import org.powbot.krulvis.mixology.Mixology

class MixPotion(script: Mixology) : Leaf<Mixology>(script, "MixPotion") {
	override fun execute() {
		var activeIngreeds = Data.Ingredient.getActiveIngredients().paired()
		val potion = script.mixToMake.first
		val requiredIngredients = potion.ingredientsPaired
		if (activeIngreeds == requiredIngredients) {
			val mixingVessel =
				Objects.stream().type(GameObject.Type.INTERACTIVE).nameContains("Mixing vessel").action("Take-from")
					.first()
			if (walkAndInteract(mixingVessel, "Take-from")) {
				waitForDistance(mixingVessel, 1000) { potion.hasUnfinished() }
			}
		} else {
			requiredIngredients.filter { it.value > 0 }.forEach { ingredient ->
				val lever = ingredient.key.getLever()
				for (i in 0 until ingredient.value) {
					lever.manipulateBounds(ingredient.key)
					script.logger.info("Interacting $i out of ${ingredient.value}")
					if (walkAndInteract(lever, "Operate")) {
						var newIngreeds: Map<Data.Ingredient, Int> = Data.Ingredient.getActiveIngredients().paired()
						waitForDistance(lever, extraWait = 2000) {
							newIngreeds = Data.Ingredient.getActiveIngredients().paired()
							newIngreeds != activeIngreeds
						}
						activeIngreeds = newIngreeds
					} else {
						return
					}
				}
			}
			val mixComplete =
				waitFor { potion.ingredients.paired() == Data.Ingredient.getActiveIngredients().paired() }
			script.logger.info("MixComplete=${mixComplete}")
		}
	}

	private fun GameObject.manipulateBounds(ingedient: Data.Ingredient) {
		if (ingedient == Data.Ingredient.Aga) {
			bounds(-32, 32, -74, -20, -72, -48)
		} else {
			bounds(-32, 32, -64, 0, -32, 32)
		}
	}
}