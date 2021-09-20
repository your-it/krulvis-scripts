package org.powbot.krulvis.orbcharger

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.orbcharger.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Orbs",
    description = "Craft orbs",
    author = "Krulvis",
    version = "1.0.1",
    markdownFileName = "Orb.md",
    priv = true,
    category = ScriptCategory.Magic
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Orb",
            allowedValues = ["AIR", "WATER", "EARTH", "FIRE"],
            defaultValue = "WATER",
            description = "Choose orb type"
        ),
        ScriptConfiguration(
            name = "Fast charge",
            description = "Re-cast charge spell",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        )
    ]
)
class OrbCrafter : ATScript() {

    val orb by lazy { Orb.valueOf(getOption("Orb")!!) }
    val fastCharge by lazy { getOption<Boolean>("Fast charge")!! }

    override fun createPainter(): ATPaint<*> = OrbPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

}

fun main() {
    OrbCrafter().startScript("127.0.0.1", "krullieman", true)
}