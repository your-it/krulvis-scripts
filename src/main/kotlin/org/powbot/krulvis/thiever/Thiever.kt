package org.powbot.krulvis.thiever

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.thiever.tree.branch.ShouldEat
import java.util.*

@ScriptManifest(
    name = "krul Thiever",
    description = "Thieves master farmer @ Farming guild",
    version = "1.0.1",
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
        )
    ]
)
class Thiever : ATScript() {
    override val painter: ATPainter<*> = ThieverPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    val food by lazy { Food.valueOf(getOption<String>("Food") ?: "TUNA") }
    val target by lazy { getOption<String>("Target Name") ?: "Master farmer" }
    val foodAmount by lazy { (getOption<Int>("Food amount") ?: 10) }

    var mobile = false

    init {
        skillTracker.addSkill(Skill.THIEVING)
    }

    fun getTarget(): Npc? {
        return Npcs.stream().name(target).nearest().firstOrNull()
    }

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
            println("Prepping next interaction")
            getTarget()?.click()
        }
    }
}

fun main() {
    Thiever().startScript(false)
}