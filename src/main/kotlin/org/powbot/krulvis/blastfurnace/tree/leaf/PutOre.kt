package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG_CLOSED
import org.powbot.krulvis.blastfurnace.COAL_BAG_OPENED
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES

class PutOre(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Put ore on belt") {


    override fun execute() {
        ATContext.turnRunOn()
        if (!Inventory.isFull() && script.filledCoalBag) {
            debug("Emptying coal bag")
            emptyCoalBag()
        }

        val gloves = Inventory.stream().id(GOLD_GLOVES).firstOrNull()
        val bankComp = Widgets.widget(Constants.BANK_WIDGET).component(Constants.BANK_ITEMS)
        val belt = Objects.stream().name("Conveyor belt").action("Put-ore-on").firstOrNull()
        debug("Found gloves=$gloves, bankComp=$bankComp, belt=$belt")
        if (belt == null) {
            debug(
                "Belt is null... beltTile=$beltTile, objects on tile=${
                    Objects.stream().at(beltTile).list().joinToString { it.name }
                }"
            )
            return
        } else {
            beltTile = belt.tile
        }

        if (gloves != null) {
            debug("Going to equip gloves..")
            val equip = gloves.interact("Wear")
            debug("Equipping gold gloves=$equip")
            if (equip) {
                waitFor { !Inventory.containsOneOf(GOLD_GLOVES) }
            }
        } else {
            debug("Executing logic to interact with belt")
            val centerPoint = belt.centerPoint()
            debug("centerPoint=$centerPoint")
            val bankContainsBeltPoint = bankComp.boundingRect().contains(centerPoint)
            debug("bankContainsBeltPoint=$bankContainsBeltPoint")
            val closedBank = Bank.close()
            debug("closedBank=$closedBank")
            if (!bankContainsBeltPoint || closedBank) {
                val hasSpecialOres =
                    Inventory.containsOneOf(
                        Ore.ADAMANTITE.id,
                        Ore.RUNITE.id,
                        Ore.IRON.id,
                        Ore.MITHRIL.id,
                        Ore.GOLD.id
                    )

                val waitForTime = if (belt.distance() > 1) Random.nextInt(7500, 8000) else 2000
                debug("Walking and interacting with belt, hasSpecialOres=$hasSpecialOres, waitForTime=$waitForTime")

                if (walkAndInteract(belt, "Put-ore-on") && waitFor(waitForTime) { !Inventory.isFull() }) {
                    if (script.filledCoalBag) {
                        sleep(600)
                        emptyCoalBag()
                    }
                    if (hasSpecialOres) {
                        script.waitForBars = true
                        if (Inventory.containsOneOf(Ore.COAL.id)) {
                            belt.interact("Put-ore-on")
                        }
                    }
                }
            } else {
                debug("Bank is still open & can't close & can't see belt")
            }
        }
    }

    fun emptyCoalBag() = Inventory.stream().id(COAL_BAG_CLOSED, COAL_BAG_OPENED).findFirst().ifPresent {
        if (it.interact("Empty") && waitFor { Inventory.containsOneOf(Ore.COAL.id) }) {
            script.filledCoalBag = false
        }
    }

    companion object {
        var beltTile: Tile = Tile(0, 0, 0)
    }
}