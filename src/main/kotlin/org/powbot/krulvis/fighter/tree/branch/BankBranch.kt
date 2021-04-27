package org.powbot.krulvis.fighter.tree.branch

import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.bank.HandleBanking
import org.powbot.krulvis.fighter.tree.leaf.bank.OpenBank

class ShouldBank(script: Fighter) : Branch<Fighter>(script, "ShouldBank") {
    override fun validate(): Boolean {
        return script.forcedBanking
    }

    override val successComponent: TreeComponent<Fighter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Fighter> = IsKilling(script)
}

class IsBankOpen(script: Fighter) : Branch<Fighter>(script, "IsBankOpen") {

    override fun validate(): Boolean {
        return script.ctx.bank.opened()
    }

    override val successComponent: TreeComponent<Fighter> = HandleBanking(script)
    override val failedComponent: TreeComponent<Fighter> = OpenBank(script)
}