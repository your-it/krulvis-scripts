package org.powbot.krulvis.smelter

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.smelter.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Smelter",
    description = "Smelt bars or cannonballs",
    author = "Krulvis",
    version = "1.1.2",
    category = ScriptCategory.Smithing
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Cannonball",
            description = "Smelt cannonballs instead",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "Ring of forging",
            description = "Equip a ring of forging",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            name = "Bar",
            description = "Bar to smelt",
            allowedValues = ["BRONZE", "SILVER", "IRON", "STEEL", "GOLD", "MITHRIL", "ADAMANTITE", "RUNITE"],
            defaultValue = "GOLD"
        )
    ]
)
class Smelter : ATScript() {
    override fun createPainter(): ATPaint<*> = SmelterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    val bar by lazy { Bar.valueOf(getOption<String>("Bar") ?: "STEEL") }
    val cannonballs by lazy { getOption<Boolean>("Cannonball") ?: false }
    val forgingRing by lazy { getOption<Boolean>("Ring of forging") ?: false }
}

fun main() {
    Smelter().startScript(false)
}