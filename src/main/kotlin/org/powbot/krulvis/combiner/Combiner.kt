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
    version = "1.0.8",
    markdownFileName = "Combiner.md",
    scriptId = "28a99f22-08e4-4222-a14b-7c9743db6b6d",
    description = "Can do Cooking, Crafting, Fletching, Smithing, Smelting"
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Inventory items",
            description = "Put the correct amount in inventory and click the button",
            optionType = OptionType.INVENTORY,
//            defaultValue = "{\"255\":14,\"227\":14}"
//            defaultValue = "[{\"id\":1391,\"interaction\":\"Use\",\"mouseX\":726,\"mouseY\":335,\"rawEntityName\":\"<col=ff9040>Battlestaff\",\"rawOpcode\":38,\"var0\":13,\"widgetId\":9764864,\"strippedName\":\"Battlestaff\"},{\"id\":571,\"interaction\":\"Use\",\"mouseX\":756,\"mouseY\":442,\"rawEntityName\":\"<col=ff9040>Battlestaff<col=ffffff> -> <col=ff9040>Water orb\",\"rawOpcode\":31,\"var0\":26,\"widgetId\":9764864,\"strippedName\":\"Battlestaff -> Water orb\"}]"
        ),
        ScriptConfiguration(
            name = "Combine Items",
            description = "Perform the Game Actions to start combining",
            optionType = OptionType.GAME_ACTIONS,
//            defaultValue = "[{\"id\":255,\"interaction\":\"Use\",\"mouseX\":684,\"mouseY\":231,\"rawEntityName\":\"<col=ff9040>Harralander\",\"rawOpcode\":38,\"var0\":0,\"widgetId\":9764864,\"name\":\"Harralander\",\"strippedName\":\"Harralander\"},{\"id\":227,\"interaction\":\"Use\",\"mouseX\":725,\"mouseY\":236,\"rawEntityName\":\"<col=ff9040>Harralander<col=ffffff> -> <col=ff9040>Vial of water\",\"rawOpcode\":31,\"var0\":1,\"widgetId\":9764864,\"name\":\"Vial of water\",\"strippedName\":\"Harralander -> Vial of water\"},{\"id\":1,\"interaction\":\"Make\",\"mouseX\":276,\"mouseY\":116,\"rawEntityName\":\"<col=ff9040>Harralander potion (unf)</col>\",\"rawOpcode\":57,\"var0\":-1,\"widgetId\":17694734,\"componentIndex\":14,\"widgetIndex\":270,\"name\":\"Harralander potion (unf)\",\"strippedName\":\"Harralander potion (unf)\"}]"
        )
    ]
)
class Combiner : ATScript() {
    override fun createPainter(): ATPaint<*> = CombinerPainter(this)

    val combineActions by lazy { getOption<List<GameActionEvent>>("Combine Items")!! }
    val items by lazy {
        getOption<Map<Int, Int>>("Inventory items")!!
    }

    val id by lazy { items.filter { it.value in 2..28 }.map { it.key }.first() }

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

    fun stoppedUsing() = Production.stoppedUsing(id)

    @com.google.common.eventbus.Subscribe
    fun onInventoryItem(e: InventoryChangeEvent) {
        if (options.all { it.configured } && items.none { it.key == e.itemId }
            && painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == e.itemId } }
            && !Bank.opened()) {
            painter.paintBuilder.trackInventoryItems(e.itemId)
        }
    }
}

fun main() {
    Combiner().startScript("127.0.0.1", "krullieman", false)
}