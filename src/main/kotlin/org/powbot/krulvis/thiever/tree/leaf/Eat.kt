package org.powbot.krulvis.thiever.tree.leaf

import org.powbot.krulvis.api.ATContext.currentHP
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.thiever.Thiever

class Eat(script: Thiever) : Leaf<Thiever>(script, "Eat food") {
    override fun execute() {
        val hp = currentHP()
        if (script.food.eat()) {
            waitFor(long()) { hp < currentHP() }
        }
    }
}