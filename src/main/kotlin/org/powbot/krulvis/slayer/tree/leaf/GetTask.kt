package org.powbot.krulvis.slayer.tree.leaf

import org.powbot.api.Condition
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.slayer.Slayer
import org.powbot.krulvis.slayer.Slayer.Companion.sanitizeMultilineText
import java.util.regex.Pattern

class GetTask(script: Slayer) : Leaf<Slayer>(script, "Getting task") {


    //NPC messages
    private val NPC_ASSIGN_MESSAGE: Pattern =
        Pattern.compile(".*(?:Your new task is to kill|You are to bring balance to)\\s*(?<amount>\\d+) (?<name>.+?)(?: (?:in|on|south of) (?:the )?(?<location>.+))?\\.")
    private val NPC_ASSIGN_BOSS_MESSAGE: Pattern =
        Pattern.compile("^(?:Excellent\\. )?You're now assigned to (?:kill|bring balance to) (?:the )?(.*) (\\d+) times.*Your reward point tally is (.*)\\.$")
    private val NPC_ASSIGN_FIRST_MESSAGE: Pattern =
        Pattern.compile("^We'll start you off (?:hunting|bringing balance to) (.*), you'll need to kill (\\d*) of them\\.$")
    private val NPC_CURRENT_MESSAGE: Pattern =
        Pattern.compile("^You're (?:still(?: meant to be)?|currently assigned to) (?:hunting|bringing balance to|kill|bring balance to|slaying) (?<name>.+?)(?: (?:in|on|south of) (?:the )?(?<location>.+))?(?:, with|; (?:you have|only)) (?<amount>\\d+)(?: more)? to go\\..*")

    override fun execute() {
        script.currentTask = null
        val widget = script.widget()
        if (!widget.valid()) {
            val masterTile = script.master.tile
            val master = script.master.master()
            if (masterTile.distance() > 10 || master?.reachable() == false) {
                Movement.walkTo(masterTile)
            } else if (master?.interact("Assignment") == true) {
                Condition.wait({ script.widget().valid() }, 500, 10)
            }
        } else {
            val textComp = widget.components()
                .firstOrNull { it.text().contains("Your new task is") } ?: return
            val text = textComp.text().sanitizeMultilineText()
            script.log.info("Parsing task from widget \n TEXT: $text")
            val assignMsg = NPC_ASSIGN_MESSAGE.matcher(text)
            val bossAssignMsg = NPC_ASSIGN_BOSS_MESSAGE.matcher(text)
            val firstAssignMsg = NPC_ASSIGN_FIRST_MESSAGE.matcher(text)
            val currentAssignMsg = NPC_CURRENT_MESSAGE.matcher(text)
            if (assignMsg.find())
                script.parseTask(assignMsg)
            else if (bossAssignMsg.find())
                script.parseTask(bossAssignMsg)
            else if (firstAssignMsg.find())
                script.parseTask(firstAssignMsg)
            else if (currentAssignMsg.find())
                script.parseTask(currentAssignMsg)
        }


    }
}