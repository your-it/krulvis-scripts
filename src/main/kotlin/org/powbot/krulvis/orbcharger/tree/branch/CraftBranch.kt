package org.powbot.krulvis.orbcharger.tree.branch

import org.powbot.api.Condition.sleep
import org.powbot.api.Production
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.orbcharger.OrbCrafter
import org.powbot.krulvis.orbcharger.tree.leaf.Charge
import org.powbot.krulvis.orbcharger.tree.leaf.Walk

class AtObelisk(script: OrbCrafter) : Branch<OrbCrafter>(script, "ShouldBank?") {
    override val successComponent: TreeComponent<OrbCrafter> = IsCrafting(script)
    override val failedComponent: TreeComponent<OrbCrafter> = Walk(script)

    override fun validate(): Boolean {
        return script.orb.obeliskTile.distance() <= 2
    }
}

class IsCrafting(script: OrbCrafter) : Branch<OrbCrafter>(script, "IsBankOpen?") {
    override val successComponent: TreeComponent<OrbCrafter> =
        SimpleLeaf(script, "Chilling?") { sleep(Random.nextInt(500, 600)) }
    override val failedComponent: TreeComponent<OrbCrafter> = Charge(script)

    override fun validate(): Boolean {
        return !script.fastCharge && !Production.stoppedMaking(script.orb.id)
    }
}