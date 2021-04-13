package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross


class GetRope(script: Tempoross) : Leaf<Tempoross>(script, "Getting rope") {
    override fun loop() {
        val ropes =
            objects.toStream().name("Ropes").filter { it.tile().distanceTo(script.mastLocation) <= 6 }.findFirst()
        if (script.interactWhileDousing(ropes, "Take", script.mastLocation, true)) {
            waitFor(long()) { inventory.contains(ROPE) }
        }
    }
}