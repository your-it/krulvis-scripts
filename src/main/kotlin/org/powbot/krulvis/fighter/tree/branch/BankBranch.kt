package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.fighter.Fighter
import org.powbot.krulvis.fighter.tree.leaf.HandleBank

class ShouldBank(script: Fighter) : Branch<Fighter>(script, "Should Bank") {
    override val successComponent: TreeComponent<Fighter> = IsBankOpen(script)
    override val failedComponent: TreeComponent<Fighter> = CanLoot(script)

    override fun validate(): Boolean {
        if (script.forcedBanking) return true

        val ammo = script.equipment.firstOrNull { it.slot == Equipment.Slot.QUIVER }
        if (ammo != null && !ammo.inEquipment()) return true

        return !Food.hasFood() && (Food.needsFood()
                || (Inventory.isFull() && !Potion.PRAYER.hasWith())
                || Bank.opened())
    }
}

class IsBankOpen(
    script: Fighter,
    override val successComponent: TreeComponent<Fighter> = HandleBank(script),
    override val failedComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Open Bank") {
        if (Game.clientState() == Constants.GAME_LOGGED) {
            script.forcedBanking = true
            if (Prayer.quickPrayer()) {
                Prayer.quickPrayer(false)
            }
            script.bank.open()
        }
    },
) : Branch<Fighter>(script, "Should Bank") {

    override fun validate(): Boolean {
        return Bank.opened()
    }
}