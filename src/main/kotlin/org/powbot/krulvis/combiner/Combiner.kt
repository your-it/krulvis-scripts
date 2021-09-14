package org.powbot.krulvis.combiner

import org.powbot.api.Production
import org.powbot.api.StringUtils
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.event.WidgetActionEvent
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.ValueChanged
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.combiner.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Combiner",
    author = "Krulvis",
    version = "1.0.0",
    markdownFileName = "Combiner.md",
    scriptId = "28a99f22-08e4-4222-a14b-7c9743db6b6d",
    description = "Can do Cooking, Crafting, Fletching, Smithing, Smelting"
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Items to combine",
            description = "Put the correct amount in inventory and interact once with each unique item",
            optionType = OptionType.INVENTORY_ITEMS,
//            defaultValue = "[{\"id\":1391,\"interaction\":\"Use\",\"mouseX\":726,\"mouseY\":335,\"rawEntityName\":\"<col=ff9040>Battlestaff\",\"rawOpcode\":38,\"var0\":13,\"widgetId\":9764864,\"strippedName\":\"Battlestaff\"},{\"id\":571,\"interaction\":\"Use\",\"mouseX\":756,\"mouseY\":442,\"rawEntityName\":\"<col=ff9040>Battlestaff<col=ffffff> -> <col=ff9040>Water orb\",\"rawOpcode\":31,\"var0\":26,\"widgetId\":9764864,\"strippedName\":\"Battlestaff -> Water orb\"}]"
        ),
        ScriptConfiguration(
            name = "Open Combine Component",
            description = "Perform the Game Actions to open the combining component",
            optionType = OptionType.GAME_ACTIONS,
//            defaultValue = "[{\"id\":1391,\"interaction\":\"Use\",\"mouseX\":726,\"mouseY\":335,\"rawEntityName\":\"<col=ff9040>Battlestaff\",\"rawOpcode\":38,\"var0\":13,\"widgetId\":9764864,\"strippedName\":\"Battlestaff\"},{\"id\":571,\"interaction\":\"Use\",\"mouseX\":756,\"mouseY\":442,\"rawEntityName\":\"<col=ff9040>Battlestaff<col=ffffff> -> <col=ff9040>Water orb\",\"rawOpcode\":31,\"var0\":26,\"widgetId\":9764864,\"strippedName\":\"Battlestaff -> Water orb\"}]"
        ),
        ScriptConfiguration(
            name = "Combine Items",
            description = "Perform the widgetActionEvent to start combining",
            optionType = OptionType.WIDGETS,
//            defaultValue = "[{\"id\":1,\"interaction\":\"Make\",\"mouseX\":276,\"mouseY\":91,\"rawEntityName\":\"<col=ff9040>Water battlestaff</col>\",\"rawOpcode\":57,\"var0\":-1,\"widgetId\":17694734,\"widget\":{\"boundingModel\":null,\"parentSet\":false,\"widget\":null},\"strippedName\":\"Water battlestaff\"}]"
        ),
        ScriptConfiguration(
            name = "Item 1 Amount",
            description = "How much of item 1 do you want in the inventory (0 is ALL)",
            optionType = OptionType.INTEGER,
            defaultValue = "14",
            visible = false
        ),
        ScriptConfiguration(
            name = "Item 2 Amount",
            description = "How much of item 2 do you want in the inventory (0 is ALL)",
            optionType = OptionType.INTEGER,
            defaultValue = "14",
            visible = false
        ),
        ScriptConfiguration(
            name = "Item 3 Amount",
            description = "How much of item 3 do you want in the inventory (0 is ALL)",
            optionType = OptionType.INTEGER,
            defaultValue = "0",
            visible = false
        )
    ]
)
class Combiner : ATScript() {
    override fun createPainter(): ATPaint<*> = CombinerPainter(this)

    val combineWidgetActionEvent by lazy { getOption<List<WidgetActionEvent>>("Combine Items")!!.firstOrNull() }
    val openWidgetActionEvents by lazy { getOption<List<GameActionEvent>>("Open Combine Component")!! }
    val items by lazy {
        getOption<List<InventoryItemActionEvent>>("Items to combine")!!.mapIndexed { i, iiae ->
            Pair(iiae.id, getItemAmount(i))
        }
    }

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    @ValueChanged("Items to combine")
    fun onItemsChange(items: ArrayList<InventoryItemActionEvent>) {
        val itemCount = items.size
        log.info("Items to combine updated, size=$itemCount")
        for (x in 1..3) {
            updateVisibility("Item $x Amount", x <= itemCount)
        }
    }

    fun getItemAmount(index: Int): Int {
        return getOption<Int>("Item ${index + 1} Amount")!!
    }


    fun stoppedUsing() = Production.stoppedUsing(items.first { it.second == 0 || it.second > 1 }.first)
}

fun main() {
    Combiner().startScript("127.0.0.1", "krullieman", false)
}