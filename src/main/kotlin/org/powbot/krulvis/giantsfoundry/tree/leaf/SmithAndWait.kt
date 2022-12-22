package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.Condition.sleep
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.mid
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class SmithAndWait(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Smith and wait") {


    override fun execute() {
        val action = script.currentAction ?: return
        val actionObj = action.getObj() ?: return
        script.log.info("Performing: $action, on obj=${actionObj.name}")

        var smithXp = Skills.experience(Skill.Smithing)
        if (interaction(actionObj, "Use")) {
            waitFor(long()) { Skills.experience(Skill.Smithing) > smithXp }
            smithXp = Skills.experience(Skill.Smithing)
            var lastXpGain = System.currentTimeMillis()
            while (action == script.currentAction && action.canPerform() && System.currentTimeMillis() - lastXpGain <= 4000) {
                script.log.info("Still performing.... lastXpChange=${System.currentTimeMillis() - lastXpGain}ms")
                if (smithXp < Skills.experience(Skill.Smithing)) {
                    script.log.info("Got new experience after ${System.currentTimeMillis() - lastXpGain}ms")
                    lastXpGain = System.currentTimeMillis()
                    smithXp = Skills.experience(Skill.Smithing)
                }
                sleep(150)
            }
            script.log.info(
                "Stopping SMITH" +
                        "\n action changed=${action != script.currentAction}," +
                        "\n canPerform=${action.canPerform()}," +
                        "\n lastXpGain was ${System.currentTimeMillis() - lastXpGain} ms ago"
            )
            script.stopActivity()
        } else {
            script.log.info("Failed to even SMITH interact...")
        }
    }

    fun interaction(obj: GameObject, action: String): Boolean {
        if (obj.distance() >= 5) {
            Movement.step(obj.tile)
            waitFor(long()) { obj.distance() <= 5 }
        }
        return obj.interact(action, useMenu = false)
    }
}