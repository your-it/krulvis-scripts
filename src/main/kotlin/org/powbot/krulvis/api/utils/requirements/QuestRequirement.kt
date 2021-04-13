package org.powbot.krulvis.api.utils.requirements

import org.powbot.krulvis.api.ATContext

class QuestRequirement(val quest: Int, val finished: Boolean) : Requirement {
    override fun hasRequirement(ctx: ATContext): Boolean {
//        return if (finished) ctx.quests.isCompleted(quest) else ctx.quests.isStarted(quest)
        TODO("Not implemented yet")
    }
}