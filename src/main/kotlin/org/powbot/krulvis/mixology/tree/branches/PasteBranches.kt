package org.powbot.krulvis.mixology.tree.branches

import org.powbot.api.Notifications
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mixology.Data
import org.powbot.krulvis.mixology.Mixology
import org.powbot.mobile.script.ScriptManager

class CanDepositPaste(script: Mixology) : Branch<Mixology>(script, "CanDeposit") {
	override val failedComponent: TreeComponent<Mixology> = SimpleLeaf(script, "OutOfSupplies") {
		Notifications.showNotification("Not enough ingredients, stopping script")
		ScriptManager.stop()
	}
	override val successComponent: TreeComponent<Mixology> = SimpleLeaf(script, "Deposit") {
		val hopper = Objects.stream().type(GameObject.Type.INTERACTIVE).name("Hopper").first()
		if (walkAndInteract(hopper, "Deposit")) {
			waitForDistance(hopper, extraWait = 5000) {
				script.mixToMake.hasStoredIngredients()
			}
		}
	}

	override fun validate(): Boolean {
		val requiredIngredients = script.mixToMake.ingredientsPaired
		val storedIngredients = Data.Ingredient.getStoredIngredients()
		val missingIngredients =
			requiredIngredients.keys.associateWith { requiredIngredients[it]!! - storedIngredients[it]!! }
				.filter { it.value > 0 }
		return missingIngredients.all { it.key.getInventoryCount() > 0 }
	}
}