package org.powbot.krulvis.combiner

import org.powbot.api.Notifications
import org.powbot.api.Production
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.InventoryItemActionEvent
import org.powbot.api.rt4.Bank
import org.powbot.api.script.*
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.combiner.tree.branch.ShouldBank
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager

@ScriptManifest(
    name = "krul Combiner",
    author = "Krulvis",
    version = "1.0.9",
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
            defaultValue = "{\"946\":1,\"1517\":27}"
//            defaultValue = "[{\"id\":1391,\"interaction\":\"Use\",\"mouseX\":726,\"mouseY\":335,\"rawEntityName\":\"<col=ff9040>Battlestaff\",\"rawOpcode\":38,\"var0\":13,\"widgetId\":9764864,\"strippedName\":\"Battlestaff\"},{\"id\":571,\"interaction\":\"Use\",\"mouseX\":756,\"mouseY\":442,\"rawEntityName\":\"<col=ff9040>Battlestaff<col=ffffff> -> <col=ff9040>Water orb\",\"rawOpcode\":31,\"var0\":26,\"widgetId\":9764864,\"strippedName\":\"Battlestaff -> Water orb\"}]"
        ),
        ScriptConfiguration(
            name = "Combine Items",
            description = "Perform the Game Actions to start combining",
            optionType = OptionType.GAME_ACTIONS,
            defaultValue = "[{\"id\":946,\"interaction\":\"Use\",\"mouseX\":869,\"mouseY\":238,\"rawEntityName\":\"<col=ff9040>Knife</col>\",\"rawOpcode\":25,\"var0\":0,\"widgetId\":9764864,\"name\":\"Knife\",\"strippedName\":\"Knife\"},{\"id\":1517,\"interaction\":\"Use\",\"mouseX\":915,\"mouseY\":230,\"rawEntityName\":\"<col=ff9040>Knife</col><col=ffffff> -> <col=ff9040>Maple logs</col>\",\"rawOpcode\":58,\"var0\":1,\"widgetId\":9764864,\"name\":\"Maple logs\",\"strippedName\":\"Knife -> Maple logs\"},{\"id\":1,\"interaction\":\"Make\",\"mouseX\":291,\"mouseY\":85,\"rawEntityName\":\"<col=ff9040>Maple longbow</col>\",\"rawOpcode\":57,\"var0\":-1,\"widgetId\":17694736,\"componentIndex\":-1,\"widgetIndex\":-1,\"name\":\"Maple longbow\",\"strippedName\":\"Maple longbow\"}]"
        ),
        ScriptConfiguration(
            name = "Spam Click",
            description = "Enable this to perform the interaction for every item",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false"
        )
    ]
)
class Combiner : ATScript() {
    override fun createPainter(): ATPaint<*> = CombinerPainter(this)

    val spamClick by lazy { getOption<Boolean>("Spam Click") }
    val combineActions by lazy { getOption<List<GameActionEvent>>("Combine Items") }
    val items by lazy {
        getOption<Map<Int, Int>>("Inventory items")
    }

    val id by lazy {
        if (items.isEmpty()) {
            Notifications.showNotification("Inventory in GUI cannot be empty!")
            log.info("Inventory in GUI cannot be empty!")
            ScriptManager.stop()
        }
        items.filter { it.value in 2..28 }.map { it.key }.first()
    }

    val name by lazy { ItemLoader.load(id)?.name }

    override val rootComponent: TreeComponent<*> = ShouldBank(this)

    @ValueChanged("Items to combine")
    fun onItemsChange(items: ArrayList<InventoryItemActionEvent>) {
        val itemCount = items.size
        log.info("Items to combine updated, size=$itemCount")
        for (x in 1..4) {
            updateVisibility("Item $x Amount", x <= itemCount)
        }
    }

    fun stoppedUsing() = Production.stoppedUsing(id)

    @com.google.common.eventbus.Subscribe
    fun onInventoryItem(e: InventoryChangeEvent) {
        if (ScriptManager.state() == ScriptState.Running && items.none { it.key == e.itemId }
            && painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == e.itemId } }
            && !Bank.opened()) {
            painter.paintBuilder.trackInventoryItems(e.itemId)
        }
    }
}

fun main() {
    Combiner().startScript("127.0.0.1", "GIM", false)
}
//Starting Script with JSON: [{"allowedValues":[],"defaultValue":{},"description":"Put the correct amount in inventory and click the button","enabled":true,"name":"Inventory items","optionType":"INVENTORY","value":{"1785":1,"1775":27},"visible":true},{"allowedValues":[],"defaultValue":[],"description":"Perform the Game Actions to start combining","enabled":true,"name":"Combine Items","optionType":"GAME_ACTIONS","value":[{"id":1785,"interaction":"Use","mouseX":685,"mouseY":248,"rawEntityName":"<col=ff9040>Glassblowing pipe","rawOpcode":38,"var0":0,"widgetId":9764864,"name":"Glassblowing pipe","strippedName":"Glassblowing pipe"},{"id":1775,"interaction":"Use","mouseX":723,"mouseY":250,"rawEntityName":"<col=ff9040>Glassblowing pipe<col=ffffff> -> <col=ff9040>Molten glass","rawOpcode":31,"var0":1,"widgetId":9764864,"name":"Molten glass","strippedName":"Glassblowing pipe -> Molten glass"},{"id":1,"interaction":"Make","mouseX":365,"mouseY":94,"rawEntityName":"<col=ff9040>Unpowered staff orb</col>","rawOpcode":57,"var0":-1,"widgetId":17694739,"componentIndex":19,"widgetIndex":270,"name":"Unpowered staff orb","strippedName":"Unpowered staff orb"}],"visible":true}]