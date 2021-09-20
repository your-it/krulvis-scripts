package org.powbot.krulvis.orbcharger.tree.leaf

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.WebWalking
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.orbcharger.OrbCrafter

class Walk(script: OrbCrafter) : Leaf<OrbCrafter>(script, "Walking") {
    override fun execute() {
        WebWalking.moveTo(
            script.orb.obeliskTile, false,
            {
                if (Combat.isPoisoned() && Potion.ANTIPOISON.inInventory() && Potion.ANTIPOISON.drink()) {
                    waitFor { !Combat.isPoisoned() }
                }
                false
            },
            1, 50, false
        )
    }
}