package org.powbot.krulvis.fighter

import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.*
import org.powbot.api.script.paint.InventoryItemPaintItem
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.extensions.BankLocation
import org.powbot.krulvis.api.extensions.BankLocation.Companion.getNearestBank
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powbot.krulvis.api.extensions.items.Food
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.tree.branch.ShouldEat
import org.powbot.krulvis.fighter.tree.branch.ShouldEquipAmmo
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.rscache.loader.ItemLoader

@ScriptManifest(
    name = "krul Fighter",
    description = "Fights anything, anywhere",
    author = "Krulvis",
    version = "1.1.3",
    markdownFileName = "Fighter.md",
    scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
    category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "inventory", "What should your inventory look like?",
            optionType = OptionType.INVENTORY
        ),
        ScriptConfiguration(
            "equipment", "What do you want to wear?",
            optionType = OptionType.EQUIPMENT
        ),
        ScriptConfiguration(
            "monsters",
            "Click the NPC's you want to kill",
            optionType = OptionType.NPC_ACTIONS
        ),
        ScriptConfiguration(
            "radius", "Kill radius", optionType = OptionType.INTEGER, defaultValue = "7"
        ),
        ScriptConfiguration(
            "Use safespot", "Do you want to force a safespot?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            "safespot", "Get safespot",
            optionType = OptionType.TILE,
            visible = false
        ),
        ScriptConfiguration(
            "Loot price", "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
        ),
        ScriptConfiguration(
            "Always loot",
            "Names to always loot separated with \",\"",
            optionType = OptionType.STRING,
            defaultValue = "Dragon bones, Blue dragonhide, Nature rune"
        ),
        ScriptConfiguration(
            "bank", "Choose bank", optionType = OptionType.STRING, defaultValue = "FALADOR_WEST_BANK",
            allowedValues = ["NEAREST", "LUMBRIDGE_TOP", "FALADOR_WEST_BANK", "FALADOR_EAST_BANK", "LUMBRIDGE_CASTLE_BANK", "VARROCK_WEST_BANK", "VARROCK_EAST_BANK", "CASTLE_WARS_BANK", "EDGEVILLE_BANK", "DRAYNOR_BANK", "SEERS_BANK", "AL_KHARID_BANK", "SHANTAY_PASS_BANK", "CANIFIS_BANK", "CATHERBY_BANK", "YANILLE_BANK", "ARDOUGNE_NORTH_BANK", "ARDOUGNE_SOUTH_BANK", "MISCELLANIA_BANK", "GNOME_STRONGHOLD_BANK", "TZHAAR_BANK", "FISHING_GUILD_BANK", "BURTHORPE_BANK", "PORT_SARIM_DB", "MOTHERLOAD_MINE", "MINING_GUILD", "MOTHERLOAD_MINE_DEPOSIT", "FARMING_GUILD_85", "FARMING_GUILD_65"]
        )
    ]
)
class Fighter : ATScript() {
    override fun createPainter(): ATPaint<*> = FighterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    val useSafespot by lazy { getOption<Boolean>("Use safespot")!! }
    val safespot by lazy { getOption<Tile>("safespot")!! }

    val inventoryOptions by lazy { getOption<Map<Int, Int>>("inventory")!! }
    val inventory by lazy { inventoryOptions.filterNot { Potion.isPotion(it.key) } }
    val potions by lazy {
        inventoryOptions.filter { Potion.isPotion(it.key) }.mapNotNull { Pair(Potion.forId(it.key), it.value) }
            .groupBy {
                it.first
            }.map { it.key!! to it.value.sumOf { pair -> pair.second } }
    }
    val equipmentOptions by lazy { getOption<Map<Int, Int>>("equipment")!! }
    val equipment by lazy {
        equipmentOptions.filterNot { TeleportItem.isTeleportItem(it.key) }.map {
            Equipment(
                emptyList(),
                if (it.value > 1) org.powbot.api.rt4.Equipment.Slot.QUIVER else null,
                it.key
            )
        }
    }
    val teleportItems by lazy {
        equipmentOptions.keys.mapNotNull {
            TeleportItem.getTeleportItem(it)
        }
    }
    val food by lazy { inventory.asSequence().mapNotNull { Food.forId(it.key) }.firstOrNull() }
    val monsters by lazy {
        getOption<List<NpcActionEvent>>("monsters")!!.map { it.name }
    }
    val radius by lazy { getOption<Int>("radius")!! }
    val minLoot by lazy { getOption<Int>("Loot price")!! }
    val lootNames by lazy {
        val names = getOption<String>("Always loot")!!.split(",").toMutableList()
        val ammo = equipment.firstOrNull { it.slot == org.powbot.api.rt4.Equipment.Slot.QUIVER }
        if (ammo != null) {
            names.add(ItemLoader.load(ammo.id)?.name ?: "nulll")
        }
        val trimmed = mutableListOf<String>()
        names.forEach { trimmed.add(it.trim()) }
        trimmed.toList()
    }
    val bank by lazy {
        val b = getOption<String>("bank")!!
        if (b == "nearest") {
            Bank.getNearestBank()
        } else {
            BankLocation.valueOf(b)
        }
    }

    var forcedBanking = false

    fun canEat() = food != null && ATContext.missingHP() > food!!.healing

    fun needFood(): Boolean = ATContext.currentHP().toDouble() / ATContext.maxHP() < .4

    fun getNearbyMonsters(): List<Npc> {
        return Npcs.stream().within(radius.toDouble()).name(*monsters.toTypedArray()).nearest().list()
    }

    fun getTarget(): Npc? {
        val local = Players.local()
        return getNearbyMonsters().filter {
            val target = it.interacting()
            (!it.healthBarVisible() || it.healthPercent() > 0) && (target == Actor.Companion.Nil || target == local)
        }.firstOrNull()
    }

    @ValueChanged("Use safespot")
    fun onValueChange(useSafespot: Boolean) {
        updateVisibility("safespot", useSafespot)
        if (!useSafespot) {
            updateOption("safespot", Tile.Nil, OptionType.TILE)
        }
    }

    fun loot(): List<GroundItem> {
        return GroundItems.stream().within(this.radius + 10.0)
            .filtered { lootNames.contains(it.name()) || GrandExchange.getItemPrice(it.id()) * it.stackSize() >= minLoot }
            .nearest().list()
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        if (!inventory.containsKey(id) && !equipmentOptions.containsKey(id) && !TeleportItem.isTeleportItem(id)) {
            if (painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
                painter.paintBuilder.trackInventoryItems(id)
                painter.paintBuilder.items.forEach { row ->
                    (row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id } as InventoryItemPaintItem).diff += evt.quantityChange
                }
            }
        }
    }

}

class FighterPainter(script: Fighter) : ATPaint<Fighter>(script) {

    override fun buildPaint(paintBuilder: PaintBuilder): Paint {
        paintBuilder
            .trackSkill(Skill.Attack)
            .trackSkill(Skill.Strength)
            .trackSkill(Skill.Defence)
            .trackSkill(Skill.Hitpoints)
            .trackSkill(Skill.Prayer)
            .trackSkill(Skill.Magic)
            .trackSkill(Skill.Ranged)

        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
    }
}

fun main() {
    Fighter().startScript("127.0.0.1", "krullieman", false)
}