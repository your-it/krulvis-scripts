package org.powbot.krulvis.chompy.tree.leaf

import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.chompy.ChompyBird

class DropToad(script: ChompyBird) : Leaf<ChompyBird>(script, "DropToad") {
    override fun execute() {
        val tile = me.tile()
        if (Inventory.stream().name("Bloated toad").first().interact("Drop")) {
            waitFor(1200) { me.tile() != tile }
        }
    }
}