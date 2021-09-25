package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.extensions.BankLocation.Companion.openNearestBank
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.HandleBank

class ShouldBank(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Fighter> = IsKilling(script)

    override fun validate(): Boolean {
        if (script.forcedBanking) return true
        val hasFood = script.food != null && Inventory.containsOneOf(*script.food!!.ids)
        return !hasFood && (script.needFood() || Inventory.isFull() || Bank.opened())
    }
}

class IsBankOpen(
    script: Fighter,
    override val successComponent: TreeComponent<Fighter> = HandleBank(script),
    override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Open Bank") {
        if (Game.clientState() == Constants.GAME_LOGGED) {
            script.forcedBanking = true
            script.bank.open()
        }
    },
) : Branch<Fighter>(script, "Should Bank") {

    override fun validate(): Boolean {
        return Bank.opened()
    }
}