package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES

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
                    Ore.ADAMANTITE.id,
                    Ore.RUNITE.id,
                    Ore.IRON.id,
                    Ore.MITHRIL.id,
                    Ore.GOLD.id
                )
            Objects.stream().name("Conveyor belt").action("Put-ore-on").findFirst().ifPresent {
                val waitForTime = if (it.distance() > 1)
                    Random.nextInt(7500, 8000)
                else 2000
                if (interact(it, "Put-ore-on") && waitFor(waitForTime) { !Inventory.isFull() }) {
                    if (script.filledCoalBag) {
                        sleep(600)
                        emptyCoalBag()
                    }
                    if (hasSpecialOres) {
                        script.waitForBars = true
                    }
                }
            }
        }

    }

    fun emptyCoalBag() = Inventory.stream().id(COAL_BAG).findFirst().ifPresent {
        if (it.interact("Empty") && waitFor { Inventory.containsOneOf(Ore.COAL.id) }) {
            script.filledCoalBag = false
            waitFor { Inventory.containsOneOf(Ore.COAL.id) }
        }
    }
}