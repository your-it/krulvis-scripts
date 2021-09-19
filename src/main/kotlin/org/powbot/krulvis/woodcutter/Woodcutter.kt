package org.powbot.krulvis.woodcutter

import org.powbot.api.Tile
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.script.*
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.antiban.OddsModifier
import org.powbot.krulvis.api.extensions.items.Item.Companion.TINDERBOX
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.woodcutter.tree.branch.ShouldBurn

@ScriptManifest(
    name = "krul Woodcutter",
    description = "Chops any tree, anywhere",
    author = "Krulvis",
    version = "1.0.0",
    scriptId = "2834ffcc-a81d-4c08-b163-84cc9c8ef130",
    category = ScriptCategory.Woodcutting
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Trees",
            optionType = OptionType.GAMEOBJECTS,
            description = "What trees do you want to chop?"
        ),
        ScriptConfiguration(
            name = "Bank",
            optionType = OptionType.BOOLEAN,
            description = "Bank the logs?"
        ),
        ScriptConfiguration(
            name = "Burn",
            optionType = OptionType.BOOLEAN,
            description = "Burn the logs?"
        )
    ]
)
class Woodcutter : ATScript() {

    val trees by lazy { getOption<List<GameObjectActionEvent>>("Trees")!!.map { it.tile.derive(1, 1) } }
    val bank by lazy { getOption<Boolean>("Bank")!! }
    val burn by lazy { getOption<Boolean>("Burn")!! }


    val TOOLS = intArrayOf(1349, 1351, 1353, 1355, 1357, 1359, 1361, 6739, 13241, 13242, 14028, TINDERBOX)
    val LOGS = intArrayOf(1511, 1513, 1515, 1517, 1519, 1521, 2862, 6332, 6333, 19669)
    val NESTS = intArrayOf(5070, 5071, 5072, 5073, 5074)
    var lastChopAnim = 0L
    var burning = true
    var burnTile: Tile? = null
    val chopDelay = DelayHandler(2000, 4000, OddsModifier(), "Chop delay")

    override fun createPainter(): ATPaint<*> = WoodcutterPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldBurn(this)

    @ValueChanged("Bank")
    fun onValueChanged(bank: Boolean) {
        updateVisibility("Burn", !bank)
        if (bank) {
            updateOption("Burn", false, OptionType.BOOLEAN)
        }
    }
}

fun main() {
    Woodcutter().startScript(false)
}