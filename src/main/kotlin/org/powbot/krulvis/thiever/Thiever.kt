package org.powbot.krulvis.thiever

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameActionOpcode
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.script.*
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.thiever.tree.branch.ShouldEat
import org.powbot.mobile.rscache.loader.ItemLoader
import org.powbot.mobile.script.ScriptManager
import java.util.*

@ScriptManifest(
    name = "krul Thiever",
    description = "Pickpockets any NPC",
    author = "Krulvis",
    version = "1.1.1",
    markdownFileName = "Thiever.md",
    scriptId = "e6043ead-e607-4385-b67a-a86dcf699204",
    category = ScriptCategory.Thieving
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Targets",
            description = "What NPC to pickpocket?",
            optionType = OptionType.NPC_ACTIONS
        ),
        ScriptConfiguration(
            name = "Food",
            description = "What food to use?",
            defaultValue = "TUNA",
            allowedValues = ["SHRIMP", "CAKES", "TROUT", "SALMON", "PEACH", "TUNA", "WINE", "LOBSTER", "BASS", "SWORDFISH", "POTATO_CHEESE", "MONKFISH", "SHARK", "KARAMBWAN"]
        ),
        ScriptConfiguration(
            name = "Food amount",
            description = "How much food to take from bank?",
            defaultValue = "5",
            optionType = OptionType.INTEGER
        ),
        ScriptConfiguration(
            name = "Droppables",
            description = "What loot should be dropped (split with ,)",
            defaultValue = "potato, onion, cabbage, tomato, marigold, nasturtium, rosemary, redberry, cadavaberry, dwellberry, guam, marrentill, tarromin, harralander",
            optionType = OptionType.STRING
        ),
        ScriptConfiguration(
            name = "Left-click",
            description = "Force left-click on pickpocket?",
            defaultValue = "false",
            optionType = OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            name = "Prepare menu",
            description = "Open menu right after pickpocketing?",
            defaultValue = "false",
            optionType = OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            name = "Dodgy necklace",
            description = "Equip dodgy necklace?",
            defaultValue = "false",
            optionType = OptionType.BOOLEAN
        ),
    ]
)
class Thiever : ATScript() {
    override fun createPainter(): ATPaint<*> = ThieverPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    override fun onStart() {
        super.onStart()
        dodgyNeck = getOption<Boolean>("Dodgy necklace")
    }

    val food by lazy { Food.valueOf(getOption<String>("Food")) }
    val target by lazy { getOption<List<NpcActionEvent>>("Targets") }
    val foodAmount by lazy { (getOption<Int>("Food amount")) }
    val prepare by lazy { (getOption<Boolean>("Prepare menu")) }
    val useMenu by lazy { !getOption<Boolean>("Left-click") }
    val droppables by lazy { getOption<String>("Droppables").split(",").map { it.trim() } }
    var dodgyNeck = false

    val dodgy = Equipment(emptyList(), org.powbot.api.rt4.Equipment.Slot.NECK, 21143)
    var mobile = false
    var lastTile = Tile.Nil

    fun getTarget(): Npc? {
        val farmerGuild = Tile(1249, 3735, 0)
        val tile = if (farmerGuild.distance() <= 40) {
            val farmLvl = Skills.realLevel(Constants.SKILLS_FARMING)
            when {
                farmLvl >= 85 -> Tile(1250, 3750, 0)
                else -> Tile(1264, 3729, 0)
            }
        } else {
            Players.local().tile()
        }
        if (tile.distance() > 10) {
            return null
        }
        return Npcs.stream().name(*target.map { it.name }.toTypedArray()).nearest(tile).firstOrNull()
    }

    @Subscribe
    fun onGameActionEvent(evt: GameActionEvent) {
        if (evt.rawOpcode == 11 || evt.opcode() == GameActionOpcode.InteractNpc) {
            if (ScriptManager.state() == ScriptState.Running && prepare && Game.singleTapEnabled())
                getTarget()?.click()
        }
    }

    @ValueChanged("Left-click")
    fun onValueChange(leftClick: Boolean) {
        if (leftClick) {
            updateOption("Prepare menu", false, OptionType.BOOLEAN)
        }
        updateVisibility("Prepare menu", !leftClick)
    }

    @Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        if (evt.quantityChange > 0 && painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
            painter.paintBuilder.trackInventoryItems(id)
            log.info("Now tracking: ${ItemLoader.load(id)?.name} adding ${evt.quantityChange} as start")
            painter.paintBuilder.items.forEach { row ->
                val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
                if (item != null) (item as InventoryItemPaintItem).diff += evt.quantityChange
            }
        }

    }
}

fun main() {
    Thiever().startScript("127.0.0.1", "GIM", false)
}