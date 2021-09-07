package org.powbot.krulvis.thiever

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.thiever.tree.branch.ShouldEat
import java.util.*

@ScriptManifest(
    name = "krul Thiever",
    description = "Pickpockets any NPC",
    version = "1.0.2",
    markdownFileName = "Thiever.md",
    category = ScriptCategory.Thieving
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Target Name",
            description = "What NPC to pickpocket?",
            defaultValue = "Master farmer"
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
            name = "Prepare menu",
            description = "Open menu right after pickpocketing?",
            defaultValue = "true",
            optionType = OptionType.BOOLEAN
        )
    ]
)
class Thiever : ATScript() {
    override fun createPainter(): ATPaint<*> = ThieverPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    val food by lazy { Food.valueOf(getOption<String>("Food") ?: "TUNA") }
    val target by lazy { getOption<String>("Target Name") ?: "Master farmer" }
    val foodAmount by lazy { (getOption<Int>("Food amount") ?: 10) }
    val prepare by lazy { (getOption<Boolean>("Prepare menu") ?: true) }

    var mobile = false

    fun getTarget(): Npc? {
        return Npcs.stream().name(target).nearest().firstOrNull()
    }

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
            if (prepare && Game.singleTapEnabled())
                getTarget()?.click()
        }
    }
}

fun main() {
    Thiever().startScript(true)
}