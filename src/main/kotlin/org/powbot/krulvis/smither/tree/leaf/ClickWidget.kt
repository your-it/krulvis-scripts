package org.powbot.krulvis.smither.tree.leaf

import org.powbot.api.rt4.Components
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.LastMade
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.smither.Smither

class ClickWidget(script: Smither) : Leaf<Smither>(script, "Click Widget") {
    override fun execute() {
        val comp = Components.stream(312).text(script.item.toString()).firstOrNull()
        val clickable = comp?.parent() ?: return
        script.log.info("Found widget with text: ${comp.text()}, parent: $clickable")
        if (clickable.interact("Smith")) {
            val notStoppedMaking = waitFor(long()) { !LastMade.stoppedUsing(script.bar.id) }
            script.log.info("After widget click notStoppedMaking=$notStoppedMaking")
        } else {
            script.log.info("Failed to click on widget...")
        }
    }
}