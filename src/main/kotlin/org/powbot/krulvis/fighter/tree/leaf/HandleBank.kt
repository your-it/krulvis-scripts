package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.emptyExcept
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.ATContext.withdrawExact
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.fighter.Fighter
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

class HandleBank(script: Fighter) : Leaf<Fighter>(script, "Handle bank") {
    override fun execute() {
        val ids = script.inventory.map { it.key }.toIntArray()
        if (!Inventory.emptyExcept(*ids)) {
            Bank.depositInventory()
        } else if (script.canEat() && script.food?.inInventory() == true) {
            script.food!!.eat()
            waitFor { script.canEat() }
        } else {
            script.inventory.forEach { (id, amount) ->
                if (!Bank.withdrawExact(id, amount) && !Bank.containsOneOf(id)) {
                    script.log.info("Stopped because no ${ItemLoader.load(id)} in bank")
                    ScriptManager.stop()
                }
            }

            if (handleEquipment() && !script.canEat() && waitFor { script.inventory.all { Inventory.getCount(it.key) == it.value } }) {
                script.forcedBanking = false
                Bank.close()
            }
        }
    }

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