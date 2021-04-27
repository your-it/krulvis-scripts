package org.powbot.krulvis.fighter.tree.leaf.fight

import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class WalkToSpot(script: Fighter) : Leaf<Fighter>(script, "Walking to spot") {
    override fun execute() {
        walk(script.profile.centerLocation)
    }
}