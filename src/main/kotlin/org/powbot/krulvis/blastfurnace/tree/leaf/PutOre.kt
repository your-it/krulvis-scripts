package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES
import org.powbot.krulvis.blastfurnace.Ore

class PutOre(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Put ore on belt") {


    override fun execute() {
        if (Bank.opened()) {
            Bank.close()
        }
        ATContext.turnRunOn()
        if (!Inventory.isFull() && script.filledCoalBag) {
            emptyCoalBag()
        }
        val gloves = Inventory.stream().id(GOLD_GLOVES).findFirst()
        if (gloves.isPresent) {
            if (gloves.get().interact("Wear")) {
                waitFor { !Inventory.containsOneOf(GOLD_GLOVES) }
            }
        } else {
            val hasSpecialOres =
                Inventory.containsOneOf(
                    Ore.ADAMANT.itemId,
                    Ore.RUNE.itemId,
                    Ore.IRON.itemId,
                    Ore.MITH.itemId,
                    Ore.GOLD.itemId
                )
            Objects.stream().name("Conveyor belt").action("Put-ore-on").findFirst().ifPresent {
                if (interact(it, "Put-ore-on") && waitFor(Random.nextInt(5000, 7500)) { !Inventory.isFull() }) {
                    if (script.filledCoalBag)
                        emptyCoalBag()
                    if (hasSpecialOres) {
                        script.waitForBars = true
                    }
                }
            }
        }

    }

    fun emptyCoalBag() = Inventory.stream().id(COAL_BAG).findFirst().ifPresent {
        if (it.interact("Empty") && waitFor { Inventory.containsOneOf(Ore.COAL.itemId) }) {
            script.filledCoalBag = false
            waitFor { Inventory.containsOneOf(Ore.COAL.itemId) }
        }
    }
}