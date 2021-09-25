package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Chat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Kill
import org.powbot.krulvis.fighter.tree.leaf.Loot

class IsKilling(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Chillings") {
        val interacting = Players.local().interacting()
        Chat.clickContinue()
        if (waitFor { interacting.healthPercent() == 0 }) {
            waitFor(Random.nextInt(3000)) { script.loot().isNotEmpty() }
        } else
            sleep(Random.nextInt(1000, 1500))
    }
    override val failedComponent: TreeComponent<Fighter> = CanLoot(script)

    override fun validate(): Boolean {
        if (Players.local().interacting().name in script.monsters) {
            return !script.useSafespot
                    || script.safespot == Players.local().tile()
                    || !Players.local().healthBarVisible()
        }
        return false
    }
}

class CanLoot(script: Fighter) : Branch<Fighter>(script, "Can loot?") {
    override val successComponent: TreeComponent<Fighter> = Loot(script)
    override val failedComponent: TreeComponent<Fighter> = AtSpot(script)

    override fun validate(): Boolean {
        return script.loot().isNotEmpty()
    }
}


class AtSpot(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = Kill(script)
    override val failedComponent: TreeComponent<Fighter> =
        SimpleLeaf(script, "Walking") { Movement.walkTo(script.safespot) }

    override fun validate(): Boolean {
        return if (script.useSafespot) script.safespot == Players.local().tile()
        else script.getTarget()?.reachable() == true
    }
}

