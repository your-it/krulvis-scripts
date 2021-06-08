package org.powbot.krulvis.tithe.tree.tree

import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.TitheFarmer
import org.powerbot.script.rt4.Constants

class Start(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Starting") {

    override fun execute() {
        if (!script.hasSeeds()) {
            if (!ctx.chat.chatting()) {
                ctx.objects.toStream(25).name("Seed table").findFirst().ifPresent {
                    if (interact(it, "Search")) {
                        waitFor { ctx.chat.chatting() }
                    }
                }
            }
            val seed = getSeedInput()
            debug("Selecting seed: $seed")
            if (ctx.chat.chatting() && ctx.chat.continueChat(seed)) {
                waitFor { script.hasSeeds() }
            }
        } else {
            ctx.objects.toStream(25).name("Farm door").findFirst().ifPresent {
                if (interact(it, "Open")) {
                    waitFor(10000) { script.getPoints() >= 0 }
                }
            }
        }
    }

    fun getSeedInput(): String {
        val farming = ctx.skills.level(Constants.SKILLS_FARMING)
        return if (farming >= 74) {
            "Golovanova"
        } else if (farming >= 54) {
            "Bologano"
        } else {
            "Loganvano"
        }
    }
}