package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry

class FillCrucible(script: GiantsFoundry) : Leaf<GiantsFoundry>(script, "Fill Crucible") {

    val widget get() = Widgets.widget(270)

    fun widgetOpen() = widget.any { it?.text() == "What metal would you like to add?" }

    fun barButton(bar: String): Component? =
        widget.firstOrNull { it?.name()?.contains(bar, ignoreCase = true) == true }

    override fun execute() {
        Bank.close()
        if (!widgetOpen()) {
            val crucible = Objects.stream().name("Crucible (empty)", "Crucible (partially full)").firstOrNull()
            script.log.info("Clicking crucible=$crucible to fill it")
            if (crucible?.interact("Fill") == true) {
                waitFor { widgetOpen() }
            }
        }
        if (widgetOpen()) {
            val bar = script.getInvBar()
            script.log.info("Going to add bar=${bar?.name()}")
            if (bar == null) return
            val barButton = barButton(bar.name())
            script.log.info("Adding bar by clicking on comp=${barButton}")
            if (barButton?.click() == true) {
                waitFor { script.correctCrucibleCount(Bar.forId(bar.id)!!) }
            }
        }
    }
}