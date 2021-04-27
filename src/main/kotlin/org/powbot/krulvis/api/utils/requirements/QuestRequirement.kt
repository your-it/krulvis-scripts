package org.powbot.krulvis.api.utils.requirements

class QuestRequirement(val quest: Int, val finished: Boolean) : Requirement {

    override fun hasRequirement(): Boolean {
//        return if (finished) ctx.quests.isCompleted(quest) else ctx.quests.isStarted(quest)
        TODO("Not implemented yet")
    }
}