package org.powbot.krulvis.daeyalt

import org.powbot.api.Condition
import org.powbot.api.Production
import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.miner.Data.SPECIAL_ATTACK_PICKS
import kotlin.random.Random

class ShouldSpecial(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "ShouldSpecial?") {
	override val successComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "Special") {
		if (Combat.specialAttack(true))
			specTimer = null
	}
	override val failedComponent: TreeComponent<DaeyaltMiner> = ShouldMine(script)

	var specTimer: Timer? = null
	override fun validate(): Boolean {
		if (Equipment.stream().id(*SPECIAL_ATTACK_PICKS).isEmpty()) return false
		if (Combat.specialPercentage() == 100 && specTimer == null) {
			specTimer = Timer(Random.nextInt(5000, 25000))
		}
		return specTimer != null && specTimer!!.isFinished()
	}

}

class ShouldMine(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "ShouldMine?") {
	override val successComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "Mine") {
		val mine = Objects.stream().name("Daeyalt Essence").action("Mine").firstOrNull() ?: return@SimpleLeaf
		if (mine.distance() > 5) {
			Movement.step(mine.tile)
			Condition.wait({ mine.distance() <= 5 }, 500, 10)
		} else if (Utils.walkAndInteract(mine, "Mine")) {
			Condition.wait({ !Production.stoppedMaking(ESSENCE) }, 500, 10)
		}
	}
	override val failedComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "Chill") {
		sleep(600)
	}

	override fun validate(): Boolean =
		Production.stoppedMaking(ESSENCE)


}
