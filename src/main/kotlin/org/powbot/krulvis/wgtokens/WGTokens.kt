package org.powbot.krulvis.wgtokens

import org.powbot.api.rt4.*
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsAll
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.wgtokens.tree.branch.ShouldEat

@ScriptManifest(
    name = "krul Tokens",
    description = "Gets tokens in Warrior's Guild",
    author = "Krulvis",
    markdownFileName = "WGTokens.md",
    version = "1.0.0"
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Inventory",
            "Have the armour-type and food in inventory before clicking",
            optionType = OptionType.INVENTORY
        )
    ]
)
class WGTokens : ATScript() {
    override fun createPainter(): ATPaint<*> {
        return WGTokensPaint(this)
    }

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    val inventoryOptions by lazy { getOption<Map<Int, Int>>("Inventory")!! }

    val armour by lazy { Armour.values().first { it.ids.any { id -> inventoryOptions.contains(id) } } }

    val food by lazy { Food.values().first { inventoryOptions.containsKey(it.id) } }

    fun loot(): List<GroundItem> = GroundItems.stream().id(*armour.ids, *tokens).list()

    val tokens = intArrayOf(8855, 8854, 8853, 8852, 8851)

    enum class Armour(vararg val ids: Int) {
        Black(1125, 1077, 1165),
        Mithril(1121, 1071, 1159),
        Adamant(1123, 1073, 1161),
        Runite(1127, 1079, 1163),
        ;

        fun inInventory() = Inventory.containsAll(*ids)

        fun npc() = Npcs.stream().name("Animated $name Armour").firstOrNull { it.interacting() == Players.local() }
    }
}

fun main() {
    WGTokens().startScript(false)
}