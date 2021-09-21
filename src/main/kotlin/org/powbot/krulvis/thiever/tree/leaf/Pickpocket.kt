package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Skills
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever

class Pickpocket(script: Thiever) : Leaf<Thiever>(script, "Pickpocket") {
    override fun execute() {
        val target = script.getTarget()
        if (target != null && Bank.close()) {
            val xp = Skills.experience(Constants.SKILLS_THIEVING)
            if (interact(target, "Pickpocket", script.useMenu)) {
                script.lastTile = target.tile()
                waitFor(Random.nextInt(4000, 5000)) { xp < Skills.experience(Constants.SKILLS_THIEVING) }
            }
        }
    }
}