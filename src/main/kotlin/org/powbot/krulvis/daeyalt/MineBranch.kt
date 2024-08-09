package org.powbot.krulvis.daeyalt

import org.powbot.api.Condition
import org.powbot.api.Notifications
import org.powbot.api.Production
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.miner.Data.SPECIAL_ATTACK_PICKS
import org.powbot.mobile.script.ScriptManager
import kotlin.random.Random


class AtMine(script: DaeyaltMiner) : Branch<DaeyaltMiner>(script, "AtMine?") {
	val mineCenter = Tile(3686, 9756, 2)
	val staircaseTile = Tile(3631, 3339, 0)
	override val failedComponent: TreeComponent<DaeyaltMiner> = SimpleLeaf(script, "WalkingToMine") {
		if (Bank.opened()) {
			Bank.close()
		}
		val stairCase = Objects.stream(20, GameObject.Type.INTERACTIVE).at(staircaseTile).name("Staircase").first()
		if (stairCase.valid()) {
			if (walkAndInteract(stairCase, "Climb-down")) {
				waitForDistance(stairCase) { validate() }
			}
		} else if (staircaseTile.distance() > 50) {
			val medallion = Equipment.stream().name("Drakan's medallion").first()
			if (!medallion.valid()) {
				Notifications.showNotification("Can't find medallion")
				ScriptManager.stop()
			} else if (medallion.interact("Darkmeyer")) {
				waitFor(long()) { staircaseTile.distance() < 50 }
			}
		} else {
			Movement.walkTo(staircaseTile)
		}
	}
	override val successComponent: TreeComponent<DaeyaltMiner> = ShouldSpecial(script)

	override fun validate(): Boolean {
		return mineCenter.distance() < 50
	}

}

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
