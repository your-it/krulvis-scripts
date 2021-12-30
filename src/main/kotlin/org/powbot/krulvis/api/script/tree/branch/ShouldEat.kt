package org.powbot.krulvis.api.script.tree.branch

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript

/**
 * ShouldEat branch usable throughout all scripts
 */
class ShouldEat<S : ATScript>(
    script: S,
    override val failedComponent: Branch<S>,
    vararg val foods: Food = Food.values()
) : Branch<S>(script, "Should eat?") {

    override val successComponent: TreeComponent<S> = SimpleLeaf(script, "Eating") {
        val count = food!!.getInventoryCount()
        if (food!!.eat()) {
            nextEatExtra = Random.nextInt(1, 8)
            Condition.wait({ food!!.getInventoryCount() < count }, 250, 15)
        }
    }

    var food: Food? = null
    var nextEatExtra = Random.nextInt(1, 8)

    companion object {
        fun needsFood(): Boolean = ATContext.currentHP().toDouble() / ATContext.maxHP().toDouble() < .4
    }

    fun food(): Food? {
        val missingHp = missingHP()
        val needsFood = needsFood()
        return foods.firstOrNull {
            it.inInventory() &&
                    (needsFood || missingHp >= it.healing + nextEatExtra)
        }
    }

    override fun validate(): Boolean {
        food = food()
        return food != null
    }
}