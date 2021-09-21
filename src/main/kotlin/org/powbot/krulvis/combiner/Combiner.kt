package org.powbot.krulvis.combiner

import org.powbot.api.Production
import org.powbot.api.StringUtils
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.event.WidgetActionEvent
import org.powbot.api.rt4.Bank
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.ValueChanged
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.combiner.tree.branch.ShouldBank

@ScriptManifest(
    name = "krul Combiner",
    author = "Krulvis",
    version = "1.0.5",
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
            name = "Combine Items",
            description = "Perform the Game Actions to start combining",
            optionType = OptionType.GAME_ACTIONS,
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
        ),
        ScriptConfiguration(
            name = "Item 4 Amount",
            description = "How much of item 4 do you want in the inventory (0 is ALL)",
            optionType = OptionType.INTEGER,
            defaultValue = "0",
            visible = false
        )
    ]
)
class Combiner : ATScript() {
    override fun createPainter(): ATPaint<*> = CombinerPainter(this)

    val combineActions by lazy { getOption<List<GameActionEvent>>("Combine Items")!! }
    val items by lazy {
        getOption<List<InventoryItemActionEvent>>("Items to combine")!!.mapIndexed { i, iiae ->
            Pair(iiae.id, getItemAmount(i))
        }
    }

    override fun onStart() {
        super.onStart()
    }

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    @ValueChanged("Items to combine")
    fun onItemsChange(items: ArrayList<InventoryItemActionEvent>) {
        val itemCount = items.size
        log.info("Items to combine updated, size=$itemCount")
        for (x in 1..4) {
            updateVisibility("Item $x Amount", x <= itemCount)
        }
    }

    fun getItemAmount(index: Int): Int {
        return getOption<Int>("Item ${index + 1} Amount")!!
    }

    fun stoppedUsing() = Production.stoppedUsing(items.first { it.second == 0 || it.second > 1 }.first)

    @com.google.common.eventbus.Subscribe
    fun onInventoryItem(e: InventoryChangeEvent) {
        if (options.all { it.configured } && items.none { it.first == e.itemId }
            && painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == e.itemId } }
            && !Bank.opened()) {
            painter.paintBuilder.trackInventoryItems(e.itemId)
        }
    }
}

fun main() {
    Combiner().startScript("127.0.0.1", "krullieman", false)
}