package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Condition.sleep
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import kotlin.math.abs

class FixTemperature(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fix temperature") {


    override fun execute() {
        val action = script.currentAction ?: return
        val targetHeat = if (action.heats) action.min else action.max
        var lastTemp = GiantsFoundry.getHeat()
        val shouldCool = lastTemp > targetHeat
        val actionObj = Objects.stream().name(if (shouldCool) "Waterfall" else "Lava pool").firstOrNull() ?: return
        val actionStr = if (shouldCool) "Cool-preform" else "Heat-preform"

        var lastStepSize = 5
        script.log.info("Performing: $actionStr, on obj=${actionObj.name}, targetHeat=$targetHeat")
        if (interaction(actionObj, "Use")) {
            waitFor { tempStep(lastTemp, lastStepSize) }
            var lastTempChangeMS = System.currentTimeMillis()

            while (!done(
                    action,
                    targetHeat,
                    shouldCool,
                    lastStepSize
                ) && System.currentTimeMillis() - lastTempChangeMS <= 3500
            ) {
                val ms = System.currentTimeMillis() - lastTempChangeMS
                script.log.info("Still fixing.... lastTempStep=${ms}ms")
                if (tempStep(lastTemp, lastStepSize)) {
                    lastStepSize = abs(lastTemp - GiantsFoundry.getHeat())
                    script.log.info("Made $lastStepSize temperature step from=$lastTemp -> ${GiantsFoundry.getHeat()}, $ms ago to")
                    lastTempChangeMS = System.currentTimeMillis()
                    lastTemp = GiantsFoundry.getHeat()
                    if (lastStepSize > 30 && abs(lastTemp - targetHeat) < lastStepSize) {
                        script.log.info("Clicking again we're making BIG steps=$lastStepSize")
                        interaction(actionObj, "Use")
                        lastStepSize = 5
                    }
                }
                sleep(150)
            }
            script.log.info(
                "Stopping FIX" +
                        "\n done=${done(action, targetHeat, shouldCool, lastStepSize)}," +
                        "\n last temp change was ${System.currentTimeMillis() - lastTempChangeMS} ms ago"
            )
            script.stopActivity(action.tile)
        } else {
            script.log.info("Failed to even FIX interact....")
        }
    }

    fun interaction(obj: GameObject, action: String): Boolean {
        if (obj.distance() >= 5) {
            Movement.step(obj.tile)
            waitFor(long()) { obj.distance() <= 5 }
        }
        obj.interact(action, useMenu = false)
        return true
    }

    fun tempStep(startTemp: Int, lastStepSize: Int) = abs(GiantsFoundry.getHeat() - startTemp) >= lastStepSize

    fun done(action: GiantsFoundry.Action, target: Int, cooling: Boolean, lastStepSize: Int): Boolean {
        val currentHeat = GiantsFoundry.getHeat()
        return if (cooling) {
            currentHeat <= if (action.heats) target + lastStepSize + 6 else target + 5
        } else {
            currentHeat >= if (action.heats) target + lastStepSize else target - lastStepSize
        }
    }
}