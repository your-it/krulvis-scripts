package org.powbot.krulvis.fighter

import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.tree.branch.ShouldBank
import org.powbot.mobile.drawing.Graphics

@ScriptManifest(
    name = "krul Fighter",
    description = "Fights anything, anywhere",
    author = "Krulvis",
    version = "1.0.1",
    category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "food", "Choose your food", defaultValue = "TUNA",
            allowedValues = ["TROUT", "SALMON", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "MONKFISH", "SHARK", "KARAMBWAN"]
        ),
        ScriptConfiguration(
            "monster", "Name of enemy to kill. Split with ',' without space", defaultValue = "Goblin,Cow"
        ),
        ScriptConfiguration(
            "max_level", "Max enemy level to attack", defaultValue = "999",
            optionType = OptionType.INTEGER
        )
    ]
)
class Fighter : ATScript() {
    override fun createPainter(): ATPaint<*> = FighterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    val food get() = Food.valueOf(getOption<String>("food")!!)

    val monsters get() = getOption<String>("monster")!!.split(",")
    val maxLevel get() = getOption<Int>("max_level")!!
}

class FighterPainter(script: Fighter) : ATPaint<Fighter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        TODO("Not yet implemented")
    }

    override fun paintCustom(g: Graphics) {
        TODO("Not yet implemented")
    }
}