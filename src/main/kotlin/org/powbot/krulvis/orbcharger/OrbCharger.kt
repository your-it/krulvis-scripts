package org.powbot.krulvis.orbcharger

import org.powbot.api.rt4.magic.Rune
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.orbcharger.tree.branch.IsPoisoned

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
            description = "Re-cast charge spell?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = "Antipoison",
            description = "Take antipoison?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            name = "Food",
            description = "Which food to use?",
            optionType = OptionType.STRING,
            defaultValue = "LOBSTER",
            allowedValues = ["SHRIMP", "CAKES", "TROUT", "SALMON", "PEACH", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "POTATO_CHEESE", "MONKFISH", "SHARK", "KARAMBWAN"]
        )
    ]
)
class OrbCrafter : ATScript() {

    val orb by lazy { Orb.valueOf(getOption("Orb")) }
    val fastCharge by lazy { getOption<Boolean>("Fast charge") }
    val antipoison by lazy { getOption<Boolean>("Antipoison") }
    val food by lazy { Food.valueOf(getOption<String>("Food")) }

    override fun createPainter(): ATPaint<*> = OrbPainter(this)

    override val rootComponent: TreeComponent<*> = IsPoisoned(this)

    val necessaries by lazy {
        intArrayOf(
            Orb.UNPOWERED,
            Orb.COSMIC,
            Rune.AIR.id,
            *Potion.ANTIPOISON.ids,
            *Potion.SUPER_ANTIPOISON.ids,
            *orb.requirements.flatMap { it.item.ids.toList() }.toIntArray()
        )
    }
}

fun main() {
    OrbCrafter().startScript("127.0.0.1", "krullieman", true)
}