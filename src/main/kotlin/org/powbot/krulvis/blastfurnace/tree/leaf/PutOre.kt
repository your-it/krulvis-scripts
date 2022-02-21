package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.api.Random
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG_CLOSED
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES

class PutOre(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Put ore on belt") {


    override fun execute() {
        ATContext.turnRunOn()
        if (!Inventory.isFull() && script.filledCoalBag) {
            emptyCoalBag()
        }
        val bankComp = Widgets.widget(Constants.BANK_WIDGET).component(Constants.BANK_ITEMS)
        val gloves = Inventory.stream().id(GOLD_GLOVES).findFirst()
        val belt = Objects.stream().name("Conveyor belt").action("Put-ore-on").firstOrNull() ?: return
        if (gloves.isPresent) {
            if (gloves.get().interact("Wear")) {
                waitFor { !Inventory.containsOneOf(GOLD_GLOVES) }
            }
        } else if (!bankComp.visible() || !bankComp.boundingRect()
                .contains(belt.centerPoint()) || Bank.close()
        ) {
            val hasSpecialOres =
                Inventory.containsOneOf(
                    Ore.ADAMANTITE.id,
                    Ore.RUNITE.id,
                    Ore.IRON.id,
                    Ore.MITHRIL.id,
                    Ore.GOLD.id
                )

            val waitForTime = if (belt.distance() > 1)
                Random.nextInt(7500, 8000)
            else 2000
            if (interact(belt, "Put-ore-on") && waitFor(waitForTime) { !Inventory.isFull() }) {
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

    fun emptyCoalBag() = Inventory.stream().id(COAL_BAG_CLOSED).findFirst().ifPresent {
        if (it.interact("Empty") && waitFor { Inventory.containsOneOf(Ore.COAL.id) }) {
            script.filledCoalBag = false
            waitFor { Inventory.containsOneOf(Ore.COAL.id) }
        }
    }
}