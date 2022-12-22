package org.powbot.krulvis.giantsfoundry.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.giantsfoundry.GiantsFoundry
import java.util.concurrent.Callable

class InteractWithObject(
    script: GiantsFoundry,
    val objectName: String,
    val action: String,
    val condition: () -> Boolean
) : Leaf<GiantsFoundry>(script, "Interacting") {

    fun getObject(): GameObject? = Objects.stream().name(objectName).firstOrNull()

    override fun execute() {
        val o = getObject()
        if (walkAndInteract(o, action)) {
            waitFor { condition() }
        }
    }
}