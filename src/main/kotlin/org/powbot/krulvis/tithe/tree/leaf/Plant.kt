package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tithe.TitheFarmer

class Plant(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Planting") {

	override fun execute() {
		val hasSeeds = script.hasSeeds()
		val patch = script.patches.firstOrNull { it.isEmpty() }
		if (!hasSeeds || patch == null) {
			script.logger.info("Stopped planting: hasSeeds=$hasSeeds, patch=$patch")
			script.planting = false
		} else {
			script.planting = true
			val seed = script.getSeed()
			if (patch.walkBetween(script.patches) && patch.plant(seed)) {
				val doneDidIt = waitForDistance(patch.tile) { !patch.isEmpty(true) }
				script.logger.info("Planted on $patch: $doneDidIt")
				if (doneDidIt) {
					if (patch.water()) {
						val tick = script.currentTick
						if (patch.index < script.patchCount - 1) {
							Inventory.stream().id(seed).findFirst().ifPresent { it.interact("Use", false) }
						}
						Condition.wait({ script.currentTick > tick + 1 }, 200, 7)
						script.logger.info("Watered as well.. $patch")
					}
				}
			}
		}
	}

}