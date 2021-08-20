package org.powbot.krulvis.smither

import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Bar
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.smither.tree.branch.ShouldBank

@ScriptManifest(
    name = "Smither",
    description = "Smiths stuff from bars",
    version = "1.0.0",
    category = ScriptCategory.Smithing
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Bar",
            description = "Bar to smith items from",
            allowedValues = ["BRONZE", "IRON", "STEEL", "MITHRIL", "ADAMANTITE", "RUNITE"],
            defaultValue = "MITHRIL"
        ),
        ScriptConfiguration(
            name = "Item",
            description = "Item to smith",
            allowedValues = ["DAGGER", "AXE", "MACE", "MEDIUM_HELM", "BOLTS", "SWORD", "DART_TIPS", "NAILS", "SCIMITAR", "ARROWTIPS", "LIMBS", "LONG_SWORD", "FULL_HELM", "KNIVES", "SQUARE_SHIELD", "WARHAMMER", "BATTLE_AXE", "CHAIN_BODY", "KITE_SHIELD", "CLAWS", "TWO_HAND_SWORD", "PLATE_SKIRT", "PLATE_LEGS", "PLATE_BODY"],
            defaultValue = "PLATE_BODY"
        )
    ]
)
class Smither : ATScript() {
    override val painter: ATPainter<*> = SmitherPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    init {
        skillTracker.addSkill(Skill.SMITHING)
    }

    val bar by lazy { Bar.valueOf(getOption<String>("Bar") ?: "STEEL") }
    val item by lazy { Smithable.valueOf(getOption<String>("Item") ?: "PLATE_BODY") }
}

fun main() {
    Smither().startScript(true)
}