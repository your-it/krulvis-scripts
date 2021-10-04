package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.Skills
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever
import kotlin.system.measureTimeMillis

class Pickpocket(script: Thiever) : Leaf<Thiever>(script, "Pickpocket") {
    override fun execute() {
        val target = script.getTarget()
        if (target != null && Bank.close()) {
            val xp = Skills.experience(Constants.SKILLS_THIEVING)
            if (interact(target, "Pickpocket", script.useMenu)) {
                script.lastTile = target.tile()
                if (waitFor(Random.nextInt(1000, 1250)) {
                        Skills.experience(Constants.SKILLS_THIEVING) > xp ||
                                Players.local().animation() == 424
                    }) {
                    if (Players.local().animation() == 424) {
                        script.log.info("Waiting to repickpocket...")
                        script.log.info(
                            "Waited for: ${
                                measureTimeMillis {
                                    sleep(Random.nextInt(4500, 5000))
                                }
                            }"
                        )
                    }
                }
            }
        }
    }
}