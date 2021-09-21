package org.powbot.krulvis.thiever

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.action.NpcAction
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.*
import org.powbot.api.script.selectors.NpcOption
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.thiever.tree.branch.ShouldEat
import java.util.*

@ScriptManifest(
    name = "krul Thiever",
    description = "Pickpockets any NPC",
    author = "Krulvis",
    version = "1.0.7",
    markdownFileName = "Thiever.md",
    scriptId = "e6043ead-e607-4385-b67a-a86dcf699204",
    category = ScriptCategory.Thieving
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Targets",
            description = "What NPC to pickpocket?",
            optionType = OptionType.NPCS
        ),
        ScriptConfiguration(
            name = "Food",
            description = "What food to use?",
            defaultValue = "TUNA",
            allowedValues = ["SHRIMP", "CAKES", "TROUT", "SALMON", "PEACH", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "POTATO_CHEESE", "MONKFISH", "SHARK", "KARAMBWAN"]
        ),
        ScriptConfiguration(
            name = "Food amount",
            description = "How much food to take from bank?",
            defaultValue = "5",
            optionType = OptionType.INTEGER
        ),
        ScriptConfiguration(
            name = "Left-click",
            description = "Force left-click on pickpocket?",
            defaultValue = "false",
            optionType = OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            name = "Prepare menu",
            description = "Open menu right after pickpocketing?",
            defaultValue = "true",
            optionType = OptionType.BOOLEAN
        ),
    ]
)
class Thiever : ATScript() {
    override fun createPainter(): ATPaint<*> = ThieverPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    val food by lazy { Food.valueOf(getOption<String>("Food") ?: "TUNA") }
    val target by lazy { getOption<List<NpcOption>>("Targets")!! }
    val foodAmount by lazy { (getOption<Int>("Food amount") ?: 10) }
    val prepare by lazy { (getOption<Boolean>("Prepare menu") ?: true) }
    val useMenu by lazy { !getOption<Boolean>("Left-click")!! }

    var mobile = false
    var lastTile = Tile.Nil

    fun getTarget(): Npc? {
        return Npcs.stream().name(*target.map { it.name }.toTypedArray()).nearest().firstOrNull()
    }

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
            if (options.firstOrNull { it.name == "Targets" }?.configured == true && prepare && Game.singleTapEnabled())
                getTarget()?.click()
        }
    }

    @ValueChanged("Left-click")
    fun onValueChange(leftClick: Boolean) {
        if (leftClick) {
            updateOption("Prepare menu", false, OptionType.BOOLEAN)
        }
        updateVisibility("Prepare menu", !leftClick)

    }
}

fun main() {
    Thiever().startScript(false)
}