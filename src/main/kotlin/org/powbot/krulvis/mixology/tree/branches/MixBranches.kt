package org.powbot.krulvis.mixology.tree.branches

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.mixology.Data
import org.powbot.krulvis.mixology.Mixology
import org.powbot.krulvis.mixology.tree.leaf.MixPotion

class HasFinishedPotion(script: Mixology) : Branch<Mixology>(script, "HasFinishedPotion") {
	override val failedComponent: TreeComponent<Mixology> = ShouldModify(script)
	override val successComponent: TreeComponent<Mixology> = SimpleLeaf(script, "HandIn") {
		val belt = Objects.stream(20, GameObject.Type.INTERACTIVE).name("Conveyor belt").first()
		val orders = Data.MixPotion.getOrders()
		if (walkAndInteract(belt, "Fulfil-order")) {
			waitForDistance(belt) { !orders.contentEquals(Data.MixPotion.getOrders()) }
		}
	}

	override fun validate(): Boolean {
		val validOrders = Data.MixPotion.getOrders()
		script.logger.info("ValidOrders=${validOrders.joinToString()}")
		script.mixToMake = validOrders.filter { it.hasLevel }.maxByOrNull { it.xp } ?: script.fallbackPot
		script.modifier = script.mixToMake.modifier()
		return script.mixToMake.hasFinishedPotion()
	}
}

class ShouldModify(script: Mixology) : Branch<Mixology>(script, "ShouldModify") {
	override val successComponent: TreeComponent<Mixology> = SimpleLeaf(script, "Modify") {
		val machine = script.modifier.getMachine()
		val modifying = me.animation() != -1
		machine.bounds(-32, 32, -64, 0, -32, 32)
		if (!modifying && walkAndInteract(machine, script.modifier.action)) {
			waitForDistance(machine, extraWait = 10000) {
				Inventory.stream().id(script.mixToMake.finishedId).isNotEmpty()
			}
		} else if (modifying) {
			script.logger.info("Waiting for modifier to finish!")
		}
	}
	override val failedComponent: TreeComponent<Mixology> = HasIngredients(script)

	override fun validate(): Boolean {
		return script.mixToMake.hasUnfinished() || script.modifier.isActive()
	}
}

class HasIngredients(script: Mixology) : Branch<Mixology>(script, "HasIngredients") {
	override val successComponent: TreeComponent<Mixology> = MixPotion(script)
	override val failedComponent: TreeComponent<Mixology> = CanDepositPaste(script)

	override fun validate(): Boolean {
		return script.mixToMake.hasStoredIngredients()
	}
}