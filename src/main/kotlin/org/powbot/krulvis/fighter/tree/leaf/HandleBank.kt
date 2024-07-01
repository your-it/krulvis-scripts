package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.missingHP
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Defender
import org.powbot.krulvis.fighter.Fighter
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Fighter) : Leaf<Fighter>(script, "Handle bank") {
    override fun execute() {
        val ids = script.requiredInventory.map { it.key }.toIntArray()
        val edibleFood = foodToEat()
        val potIds = script.requiredPotions.flatMap { it.first.ids.toList() }.toIntArray()
        val defender = Defender.defender()
        if (!Inventory.emptyExcept(Defender.defenderId(), *ids, *potIds, script.warriorTokens)) {
            Bank.depositInventory()
        } else if (edibleFood != null) {
            edibleFood.eat()
            waitFor { foodToEat() == null }
        } else if (Defender.defenderId() > 0 && !defender.hasWith()) {
            defender.withdrawExact(1)
        } else if (script.warriorGuild && !Inventory.containsOneOf(script.warriorTokens)) {
            if (!Bank.withdraw(script.warriorTokens, Bank.Amount.ALL) && !Bank.containsOneOf(script.warriorTokens)) {
                script.logger.info("Out of warrior tokens, stopping script")
                ScriptManager.stop()
            }
        } else {
            script.requiredInventory.forEach { (id, amount) ->
                if (!Bank.withdrawExact(id, amount) && !Bank.containsOneOf(id)) {
                    script.logger.info("Stopped because no ${ItemLoader.lookup(id)} in bank")
                    ScriptManager.stop()
                }
            }
            script.requiredPotions.forEach { (potion, amount) ->
                if (!potion.withdrawExact(amount) && !potion.inBank()) {
                    script.logger.info("Stopped because no $potion in bank")
                    ScriptManager.stop()
                }
            }

            if (handleEquipment()
                && foodToEat() == null
                && waitFor { script.requiredInventory.all { Inventory.getCount(it.key) == it.value } }
                && waitFor { script.requiredPotions.all { it.first.getInventoryCount() == it.second } }
            ) {
                script.forcedBanking = false
                Bank.close()
            }
        }
    }

    fun foodToEat() = Food.values().firstOrNull { it.inInventory() && it.healing <= missingHP() }

    fun handleEquipment(): Boolean {
        script.equipment.filterNot { it.slot == Equipment.Slot.QUIVER }.forEach {
            if (!it.inEquipment()) {
                it.withdrawAndEquip(true)
            }
        }
        script.teleportItems.forEach {
            if (!it.inEquipment()) {
                it.withdrawAndEquip(true)
            }
        }
        return script.equipment.all { it.inEquipment() } && script.teleportItems.all { it.inEquipment() }
    }
}