package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.TitheFarmer

class Start(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Starting") {

    override fun execute() {
        if (!script.hasSeeds()) {
            debug("Getting seeds")
            if (!chatting()) {
                val table = Objects.stream(25).name("Seed table").findFirst()
                debug("Interacting with seed table=${table.isPresent}")
                table.ifPresent {
                    if (interact(it, "Search", useMenu = false)) {
                        waitFor { chatting() }
                    }
                }
            }
            val seed = getSeedInput()
            debug("Selecting seed: $seed")
            if (chatting() && Chat.continueChat(seed)) {
                waitFor { script.hasSeeds() }
            }
        } else {
            debug("Interacting with door")
            Objects.stream(25).name("Farm door").findFirst().ifPresent {
                if (interact(it, "Open", useMenu = false)) {
                    waitFor(10000) { script.getPoints() >= 0 }
                }
            }
        }
    }

    fun chatting() = Widgets.component(Constants.CHAT_WIDGET, 0).visible()

    fun getSeedInput(): String {
        val farming = Skills.level(Constants.SKILLS_FARMING)
        return if (farming >= 74) {
            "Logavano"
        } else if (farming >= 54) {
            "Bologano"
        } else {
            "Golovanova"
        }
    }
}