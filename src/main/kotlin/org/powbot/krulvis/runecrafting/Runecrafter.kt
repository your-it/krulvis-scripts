package org.powbot.krulvis.runecrafting

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.runecrafting.tree.branches.ShouldRepair

@ScriptManifest(name = "krul Runecrafter", description = "Crafts astral runes", scriptId = "329bdd0e-3813-4c39-917b-d943e79a0f47")
@ScriptConfiguration.List([
    ScriptConfiguration(name = RUNE_ALTAR_CONFIGURATION, description = "Which rune to make?", optionType = OptionType.STRING, allowedValues = arrayOf(ASTRAL), defaultValue = ASTRAL),
    ScriptConfiguration(name = ESSENCE_TYPE_CONFIGURATION, description = "Which essence to use?", optionType = OptionType.STRING, allowedValues = arrayOf(RUNE_ESSENCE, PURE_ESSENCE, DAYALT_ESSENCE), defaultValue = PURE_ESSENCE),
])
class Runecrafter : ATScript() {

    val alter by lazy { RuneAltar.valueOf(getOption(RUNE_ALTAR_CONFIGURATION)) }
    val essence by lazy { getOption<String>(ESSENCE_TYPE_CONFIGURATION) }
    override fun createPainter(): ATPaint<*> = RCPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldRepair(this)
}

fun main() {
    Runecrafter().startScript("127.0.0.1", "GIM", true)
}