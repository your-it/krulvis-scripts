package org.powbot.krulvis.blastfurnace.tree.leaf


import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.ICE_GLOVES

class TakeBars(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Take bars") {


    override fun execute() {
        val gloves = Inventory.stream().id(ICE_GLOVES).findFirst()
        if (gloves.isPresent) {
            if (gloves.get().interact("Wear")) {
                waitFor { !Inventory.containsOneOf(ICE_GLOVES) }
            }
        } else if (takeWidgetOpen()) {
            val clickComponent = Components.stream(270).max(Comparator.comparingInt(Component::componentCount))
            clickComponent.ifPresent {
                if (it.click() && waitFor { Inventory.isFull() }) {
                    script.waitForBars = false
                }
            }
        } else if (Chat.canContinue()) {
            Chat.clickContinue()
        } else if (Bank.close()) {
            val matrix = script.dispenserTile.matrix()
            if (script.interact(matrix, "Take")) {
                waitFor(long()) { takeWidgetOpen() }
            }
        }
    }

    fun takeWidgetOpen() = Widgets.widget(270).valid()

}