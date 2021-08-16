package org.powbot.krulvis.thieving

import com.google.common.eventbus.Subscribe
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Npcs
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.ScriptProfile
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.thieving.tree.branch.ShouldEat
import java.util.*

@ScriptManifest(
    name = "krul Thiever",
    description = "Thieves master farmer @ Farming guild",
    version = "1.0.0",
    markdownFileName = "Thiever.md",
    category = ScriptCategory.Thieving
)
class Thiever : ATScript() {
    override val painter: ATPainter<*> = ThieverPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    var profile = ThieverProfile()
    var mobile = false

    init {
        skillTracker.addSkill(Skill.THIEVING)
    }

    fun getTarget(): Npc? {
        return Npcs.stream().name(profile.targetName).nearest().firstOrNull()
    }

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
            println("Prepping next interaction")
            getTarget()?.click()
        }
    }
}

data class ThieverProfile(
    val targetName: String = "Master Farmer",
    val food: Food = Food.TUNA
) : ScriptProfile