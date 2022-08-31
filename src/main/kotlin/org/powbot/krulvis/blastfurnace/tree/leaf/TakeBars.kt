package org.powbot.krulvis.blastfurnace.tree.leaf


import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.ICE_GLOVES

class TakeBars(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Take bars") {

    fun quantity(widget: Widget, quantity: Quantity): Boolean {
        val comp = widget.components().firstOrNull { it.actions().contains(quantity.action) }
        return comp == null || comp.click()
    }

    enum class Quantity(val action: String) {
        ONE("1"),
        FIVE("5"),
        TEN("10"),
        X("Other quantity"),
        ALL("All")
    }

    override fun execute() {
        val gloves = Inventory.stream().id(ICE_GLOVES).findFirst()
        val takeWidget = takeWidget()
        Bank.close()
        if (gloves.isPresent) {
            if (gloves.get().interact("Wear")) {
                waitFor { !Inventory.containsOneOf(ICE_GLOVES) }
            }
        } else if (takeWidget.valid()) {
            if (!quantity(takeWidget, Quantity.ALL)) return

            val clickComp = takeWidget.components().maxByOrNull { it.componentCount() }
            script.log.info("Clicking on Widget[${clickComp?.widgetId()}], $clickComp")
            if (clickComp != null && clickComp.click() && waitFor { Inventory.isFull() }) {
                script.waitForBars = false
            }
        } else if (Chat.canContinue()) {
            script.log.info("Can continue before taking bars.")
            Chat.clickContinue()
        } else if (Bank.close()) {
            val dispenserMatrix = script.dispenserTile.matrix()
            if (script.interact(dispenserMatrix, "Take")) {
                waitFor(long()) { takeWidget().valid() }
            }
        }
    }

    fun takeWidget() = Widgets.widget(270)

}