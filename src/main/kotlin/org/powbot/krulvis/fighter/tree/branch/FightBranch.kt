package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Players
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.Kill

class AtSpot(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = IsKilling(script)
    override val failedComponent: TreeComponent<Fighter> =
        SimpleLeaf(script, "Walking") { Movement.walkTo(script.safespot) }

    override fun validate(): Boolean {
        return script.safespot == Players.local().tile()
    }
}

class IsKilling(
    script: Fighter,
    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Chillings") {
        sleep(Random.nextInt(1000, 1500))
    },
    override val failedComponent: TreeComponent<Fighter> = Kill(script),
) : Branch<Fighter>(script, "Should Bank") {

    override fun validate(): Boolean {
        return Players.local().interacting()?.name in script.monsters
    }
}