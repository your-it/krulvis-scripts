package org.powbot.krulvis.slayer

import org.powbot.api.event.MessageEvent
import org.powbot.api.event.RenderEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.slayer.task.Dungeon.*
import org.powbot.krulvis.slayer.task.Master
import org.powbot.krulvis.slayer.task.SlayerTarget
import org.powbot.krulvis.slayer.task.SlayerTask
import org.powbot.krulvis.slayer.tree.branch.HasTask
import org.powbot.mobile.drawing.Graphics
import java.util.regex.Matcher
import java.util.regex.Pattern


@ScriptManifest(
    name = "krul Slayer",
    author = "Rob & Krulvis",
    priv = true,
    version = "1.0.0",
    description = "Trains slayer"
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Slayer Master",
            //Fuck "KRYSTILIA" for now
            allowedValues = ["TURAEL", "SPRIA", "MAZCHNA", "VANNAKA", "CHAELDAR", "KONAR", "NIEVE", "STEVE", "DURADEL"],
            description = "What slayer master do you want to use?",
            defaultValue = "VANNAKA"
        ),
        ScriptConfiguration(
            name = "Inventory",
            description = "What should the inventory look like before heading out?",
            optionType = OptionType.INVENTORY,
            defaultValue = "{\"4155\":1,\"379\":15}"
        )
    ]
)
class Slayer : ATScript() {

    var currentTarget: Npc? = null
    var currentTask: SlayerTask? = null
    val master: Master by lazy { Master.valueOf(getOption("Slayer Master")) }
    val inventory: Map<Int, Int> by lazy { getOption("Inventory") }

    //Widget info
    val WIDGET_ID = 231
    fun widget(): Widget = Widgets.widget(WIDGET_ID)

    //Reward UI
    private val REWARD_POINTS: Pattern = Pattern.compile("Reward points: ((?:\\d+,)*\\d+)")

    //Chat messages
    private val CHAT_GEM_PROGRESS_MESSAGE =
        Pattern.compile("^(?:You're assigned to kill|You have received a new Slayer assignment from .*:) (?:[Tt]he )?(?<name>.+?)(?: (?:in|on|south of) (?:the )?(?<location>[^;]+))?(?:; only | \\()(?<amount>\\d+)(?: more to go\\.|\\))$")
    private val CHAT_GEM_COMPLETE_MESSAGE = "You need something new to hunt."
    private val CHAT_COMPLETE_MESSAGE =
        Pattern.compile("You've completed (?:at least )?(?<tasks>[\\d,]+) (?:Wilderness )?tasks?(?: and received \\d+ points, giving you a total of (?<points>[\\d,]+)| and reached the maximum amount of Slayer points \\((?<points2>[\\d,]+)\\))?")
    private val CHAT_CANCEL_MESSAGE = "Your task has been cancelled."
    private val CHAT_CANCEL_MESSAGE_JAD = "You no longer have a slayer task as you left the fight cave."
    private val CHAT_CANCEL_MESSAGE_ZUK = "You no longer have a slayer task as you left the Inferno."
    private val CHAT_SUPERIOR_MESSAGE = "A superior foe has appeared..."
    private val CHAT_BRACELET_SLAUGHTER = "Your bracelet of slaughter prevents your slayer"
    private val CHAT_BRACELET_EXPEDITIOUS = "Your expeditious bracelet helps you progress your"
    private val COMBAT_BRACELET_TASK_UPDATE_MESSAGE =
        Pattern.compile("^You still need to kill (\\d+) monsters to complete your current Slayer assignment")

    @com.google.common.eventbus.Subscribe
    fun messageReceived(msg: MessageEvent) {
        val txt = msg.message.sanitizeMultilineText()
        log.info("messageReceived() \n Type: ${msg.type} \n TXT: $txt")
        val matcher = CHAT_GEM_PROGRESS_MESSAGE.matcher(txt)
        if (matcher.find())
            parseTask(matcher)
    }

    /**
     * Parse the task from the chat component
     */
    fun parseTask(matcher: Matcher) {
        log.info("Parsing task: $matcher")
        val name: String = matcher.group("name")
        val amount: Int = matcher.group("amount").toInt()
        try {
            setTask(name, amount, matcher.group("location"))
        } catch (e: NullPointerException) {
            log.info("No location specified!")
            setTask(name, amount, "")
        }
    }

    private fun setTask(name: String, amount: Int, locText: String) {
        val target = SlayerTarget.forName(name)
        if (target == null) {
            log.info("Got unparsable npc target: $name")
            return
        }
        val location = target.location(locText)
        val task = SlayerTask(target, amount, location)
        log.info("New task: $task")
        currentTask = task
    }

    override val rootComponent: TreeComponent<*> = HasTask(this)

    override fun createPainter(): ATPaint<*> {
        return Painter(this)
    }

    companion object {
        private val TAG_REGEXP = Pattern.compile("<[^>]*>")
        private val PROGRESS_VARP = 394

        private fun removeTags(str: String): String {
            return TAG_REGEXP.matcher(str).replaceAll("")
        }

        /**
         * Replaces all &lt;br&gt; delimited text with spaces and all multiple continuous
         * spaces with single space
         *
         * @param str The string to sanitize
         * @return sanitized string
         */
        fun String.sanitizeMultilineText(): String {
            return removeTags(
                replace("-<br>".toRegex(), "-")
                    .replace("<br>".toRegex(), " ")
                    .replace("[ ]+".toRegex(), " ")
            )
        }

        fun taskRemainder(): Int = Varpbits.varpbit(PROGRESS_VARP)
    }
}

fun main() {
    Slayer().startScript("localhost", "GIM", false)
}
