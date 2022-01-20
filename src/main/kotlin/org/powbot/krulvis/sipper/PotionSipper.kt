package org.powbot.krulvis.sipper

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.options.ScriptConfigurationOption
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion

@ScriptManifest(
    name = "Potion Sipper",
    author = "Krulvis",
    version = "1.0.0",
    description = "Sips all potions and uses Quick prayer (useful for AFK spots)"
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Extra info",
            description = "When you have pray pots, this script will active quick prayer. \n So make sure you have the quick prayer set to the prayers you want to use.",
            optionType = OptionType.INFO,
            defaultValue = "A"
        ),
        ScriptConfiguration(
            name = "Extra info pots",
            description = "If a potion is not sipped, it is not supported. Just tag me in discord to add it.",
            optionType = OptionType.INFO,
            defaultValue = "A"
        )
    ]
)
class PotionSipper : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return PotionPainter(this)
    }

    override val rootComponent: TreeComponent<*> = ShouldSipPotion(this, ShouldPray(this))

    class ShouldPray(script: PotionSipper) : Branch<PotionSipper>(script, "Should pray") {
        override val failedComponent: TreeComponent<PotionSipper> = SimpleLeaf(script, "Chill") {}
        override val successComponent: TreeComponent<PotionSipper> = SimpleLeaf(script, "Flick pray") {
            Prayer.quickPrayer(true)
        }

        override fun validate(): Boolean {
            return Potion.PRAYER.hasWith() && !Prayer.quickPrayer()
        }

    }
}

class PotionPainter(script: PotionSipper) : ATPaint<PotionSipper>(script) {
    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        return paintBuilder
            .trackSkill(Skill.Attack)
            .trackSkill(Skill.Strength)
            .trackSkill(Skill.Defence)
            .trackSkill(Skill.Hitpoints)
            .trackSkill(Skill.Slayer)
            .trackSkill(Skill.Magic)
            .trackSkill(Skill.Ranged)
            .build()
    }
}

fun main() {
    PotionSipper().startScript()
}