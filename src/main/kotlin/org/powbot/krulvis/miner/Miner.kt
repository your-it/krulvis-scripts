package org.powbot.krulvis.miner

import org.powbot.api.Condition
import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.*
import org.powbot.api.script.selectors.GameObjectOption
import org.powbot.krulvis.api.script.ATScript
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.antiban.DelayHandler
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.extensions.items.Ore.Companion.getOre
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.miner.tree.branch.ShouldFixStrut

@ScriptManifest(
    name = "krul Miner",
    description = "Mines & banks anything, anywhere",
    version = "1.2.0",
    markdownFileName = "Miner.md",
    category = ScriptCategory.Mining
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Rocks",
            "Which rocks do you want to mine?",
            optionType = OptionType.GAMEOBJECTS,
            //Motherlode < 72
//            defaultValue = "[{\"name\":\"Rock\",\"tile\":{\"x\":3728,\"y\":5674,\"floor\":0,\"p\":{\"x\":3728,\"y\":5674,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5672,\"floor\":0,\"p\":{\"x\":3727,\"y\":5672,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5671,\"floor\":0,\"p\":{\"x\":3727,\"y\":5671,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5670,\"floor\":0,\"p\":{\"x\":3727,\"y\":5670,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5668,\"floor\":0,\"p\":{\"x\":3727,\"y\":5668,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5667,\"floor\":0,\"p\":{\"x\":3727,\"y\":5667,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5666,\"floor\":0,\"p\":{\"x\":3727,\"y\":5666,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3728,\"y\":5664,\"floor\":0,\"p\":{\"x\":3728,\"y\":5664,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3728,\"y\":5663,\"floor\":0,\"p\":{\"x\":3728,\"y\":5663,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3727,\"y\":5661,\"floor\":0,\"p\":{\"x\":3727,\"y\":5661,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3728,\"y\":5659,\"floor\":0,\"p\":{\"x\":3728,\"y\":5659,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3728,\"y\":5658,\"floor\":0,\"p\":{\"x\":3728,\"y\":5658,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3723,\"y\":5662,\"floor\":0,\"p\":{\"x\":3723,\"y\":5662,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3723,\"y\":5663,\"floor\":0,\"p\":{\"x\":3723,\"y\":5663,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3723,\"y\":5664,\"floor\":0,\"p\":{\"x\":3723,\"y\":5664,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3723,\"y\":5666,\"floor\":0,\"p\":{\"x\":3723,\"y\":5666,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3723,\"y\":5667,\"floor\":0,\"p\":{\"x\":3723,\"y\":5667,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3724,\"y\":5669,\"floor\":0,\"p\":{\"x\":3724,\"y\":5669,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3724,\"y\":5670,\"floor\":0,\"p\":{\"x\":3724,\"y\":5670,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3725,\"y\":5674,\"floor\":0,\"p\":{\"x\":3725,\"y\":5674,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3726,\"y\":5681,\"floor\":0,\"p\":{\"x\":3726,\"y\":5681,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3730,\"y\":5679,\"floor\":0,\"p\":{\"x\":3730,\"y\":5679,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3730,\"y\":5678,\"floor\":0,\"p\":{\"x\":3730,\"y\":5678,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3730,\"y\":5677,\"floor\":0,\"p\":{\"x\":3730,\"y\":5677,\"z\":0}},\"interaction\":\"\"},{\"name\":\"Rock\",\"tile\":{\"x\":3725,\"y\":5675,\"floor\":0,\"p\":{\"x\":3725,\"y\":5675,\"z\":0}},\"interaction\":\"\"}]"
            //Motherlode >= 72
            defaultValue = "[{\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5678,\"z\":0},\"x\":3756,\"y\":5678}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5679,\"z\":0},\"x\":3756,\"y\":5679}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3758,\"y\":5680,\"z\":0},\"x\":3758,\"y\":5680}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3758,\"y\":5681,\"z\":0},\"x\":3758,\"y\":5681}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3755,\"y\":5681,\"z\":0},\"x\":3755,\"y\":5681}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3755,\"y\":5682,\"z\":0},\"x\":3755,\"y\":5682}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3755,\"y\":5683,\"z\":0},\"x\":3755,\"y\":5683}}, {\"interaction\":\"Examine\",\"name\":\"Depleted vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3756,\"y\":5684,\"z\":0},\"x\":3756,\"y\":5684}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3757,\"y\":5684,\"z\":0},\"x\":3757,\"y\":5684}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3758,\"y\":5685,\"z\":0},\"x\":3758,\"y\":5685}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3759,\"y\":5682,\"z\":0},\"x\":3759,\"y\":5682}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3761,\"y\":5681,\"z\":0},\"x\":3761,\"y\":5681}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3762,\"y\":5682,\"z\":0},\"x\":3762,\"y\":5682}}, {\"interaction\":\"Mine\",\"name\":\"Ore vein\",\"tile\":{\"floor\":0,\"p\":{\"x\":3762,\"y\":5683,\"z\":0},\"x\":3762,\"y\":5683}}]"
        ),
        ScriptConfiguration(
            "Bank ores",
            "Bank the ores you mine?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "true"
        ),
        ScriptConfiguration(
            "Hop",
            "Hop from players?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
        ScriptConfiguration(
            "Fast mine",
            "Don't sleep",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        ),
    ]
)
class Miner : ATScript() {

    init {
        skillTracker.addSkill(Skill.MINING)
    }

    val rockLocations by lazy {
        val o = getOption<List<GameObjectOption>>("Rocks")
        log.info(o.toString())
        o?.map { it.tile } ?: emptyList()
    }
    val bankOres by lazy { getOption<Boolean>("Bank ores") ?: true }
    val fastMine by lazy { getOption<Boolean>("Fast mine") ?: true }
    val hopFromPlayers by lazy { getOption<Boolean>("Hop") ?: false }

    val mineDelay = DelayHandler(2000, oddsModifier, "MineDelay")
    var lastPayDirtDrop = 0L

    override val painter: ATPainter<*> = MinerPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldFixStrut(this)

    /**
     * Used to get the amount of loot in the motherload sack
     */
    fun getMotherloadCount(): Int = (Varpbits.varpbit(375) and 65535) / 256

    var shouldEmptySack = false

    /**
     * Get the sack in the motherload mine
     */
    fun getSack() = Objects.stream().name("Sack").action("Search").findFirst()

    fun getBrokenStrut() = Objects.stream().name("Broken strut").nearest().firstOrNull()

    @com.google.common.eventbus.Subscribe
    fun onInventoryChangeEvent(evt: InventoryChangeEvent) {
        val item = evt.itemId
        if (item != Ore.PAY_DIRT.id && (item.getOre() != null || item == 21341) && evt.quantityChange > 0) {
            lootTracker.addLoot(item, evt.quantityChange)
        }
    }

}

fun main() {
    Miner().startScript(true)
}