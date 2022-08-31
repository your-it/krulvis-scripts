package org.powbot.krulvis.smelter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.extensions.items.Item.Companion.AMMO_MOULD
import org.powbot.krulvis.api.extensions.items.Item.Companion.HAMMER
import org.powbot.krulvis.api.extensions.items.Item.Companion.RING_OF_FORGING
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.smelter.Smelter
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Smelter) : Leaf<Smelter>(script, "Handling Bank") {
    override fun execute() {
        val reqs = getRequirements()
        if (!Inventory.emptyExcept(*reqs)) {
            Bank.depositAllExcept(*reqs)
        }
        val primCount = script.bar.primary.getCount()
        val split = script.bar.getOptimalSmelInventory()
        val hasSecondary = script.bar.secondaryMultiplier >= 1

        if (script.cannonballs) {
            if (!Inventory.containsOneOf(AMMO_MOULD)) {
                if (!Bank.containsOneOf(AMMO_MOULD)) {
                    script.log.info("No ammo mould found, stopping script")
                    ScriptManager.stop()
                } else {
                    Bank.withdraw(AMMO_MOULD, 1)
                }
            } else if (!Inventory.isFull()) {
                if (!Bank.containsOneOf(Bar.STEEL.id)) {
                    script.log.info("Out of steel bars, stopping script")
                    ScriptManager.stop()
                } else {
                    Bank.withdraw(Bar.STEEL.id, Bank.Amount.ALL)
                }
            }
        } else if (script.forgingRing && !ringOfForging.inEquipment()) {
            ringOfForging.withdrawAndEquip(true)
        } else {
            if (primCount != split.first) {
                if (!Inventory.isFull() && !Bank.containsOneOf(script.bar.primary.id)) {
                    script.log.info("Out of ${script.bar.primary}, stopping script")
                    ScriptManager.stop()
                } else if (primCount > split.first) {
                    Bank.deposit(script.bar.primary.id, Bank.Amount.ALL)
                } else {
                    Bank.withdraw(script.bar.primary.id, if (hasSecondary) split.first - primCount else 0)
                }
            }
            if (hasSecondary && !Inventory.isFull()) {
                if (!Inventory.isFull() && !Bank.containsOneOf(script.bar.secondary.id)) {
                    script.log.info("Out of ${script.bar.secondary}, stopping script")
                    ScriptManager.stop()
                } else if (primCount > split.second) {
                    Bank.deposit(script.bar.secondary.id, Bank.Amount.ALL)
                } else if (Bank.withdraw(script.bar.secondary.id, Bank.Amount.ALL)) {
                    Bank.close()
                }
            }
        }
    }

    val ringOfForging =
        org.powbot.krulvis.api.extensions.items.Equipment(emptyList(), Equipment.Slot.RING, RING_OF_FORGING)

    fun getRequirements(): IntArray {
        val requirements = mutableListOf(script.bar.primary.id)
        if (script.bar.secondaryMultiplier > 0) {
            requirements.add(script.bar.secondary.id)
        }
        if (script.cannonballs) {
            requirements.addAll(arrayOf(AMMO_MOULD, Bar.STEEL.id))
        }
        return requirements.toIntArray()
    }
}
