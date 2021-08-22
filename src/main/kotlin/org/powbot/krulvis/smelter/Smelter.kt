package org.powbot.krulvis.smelter

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.smelter.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Smelter",
    description = "Smelt bars or cannonballs",
    version = "1.0.0",
    category = ScriptCategory.Smithing
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Bar",
            description = "Bar to smelt",
            allowedValues = ["BRONZE", "SILVER", "IRON", "STEEL", "GOLD", "MITHRIL", "ADAMANTITE", "RUNITE"],
            defaultValue = "GOLD"
        ),
        ScriptConfiguration(
            name = "Cannonball",
            description = "Smelt cannonballs instead",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        )
    ]
)
class Smelter : ATScript() {
    override val painter: ATPainter<*> = SmelterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    init {
        skillTracker.addSkill(Skill.SMITHING)
    }

    val bar by lazy { Bar.valueOf(getOption<String>("Bar") ?: "STEEL") }
    val cannonballs by lazy { getOption<Boolean>("Cannonball") ?: false }
}

fun main() {
    Smelter().startScript(false)
}