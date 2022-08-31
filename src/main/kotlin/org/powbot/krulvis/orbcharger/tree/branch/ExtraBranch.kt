package org.powbot.krulvis.orbcharger.tree.branch

import org.powbot.api.rt4.Combat
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.orbcharger.OrbCrafter

class IsPoisoned(script: OrbCrafter) : Branch<OrbCrafter>(script, "IsPoisoned?") {
    
    override val successComponent: TreeComponent<OrbCrafter> = SimpleLeaf(script, "Drink antipot") {
        if (Potion.getAntipot()?.drink() == true) {
            waitFor { !Combat.isPoisoned() }
        }
    }
    override val failedComponent: TreeComponent<OrbCrafter> = ShouldBank(script)

    override fun validate(): Boolean {
        return Combat.isPoisoned() && Potion.hasAntipot()
    }
}