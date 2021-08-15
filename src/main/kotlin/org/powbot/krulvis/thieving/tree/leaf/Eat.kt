package org.powbot.krulvis.thieving.tree.leaf

import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thieving.Thiever

class Eat(script: Thiever) : Leaf<Thiever>(script, "Eat food") {
    override fun execute() {
        val hp = currentHP()
        if (script.profile.food.eat()) {
            waitFor(long()) { hp < currentHP() }
        }
    }
}