package org.powbot.krulvis.fighter

import org.powbot.api.Tile
import org.powbot.api.event.InventoryChangeEvent
import org.powbot.api.event.NpcActionEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
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
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportItem
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.fighter.tree.branch.ShouldEat
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.rscache.loader.ItemLoader

@ScriptManifest(
    name = "krul Fighter",
    description = "Fights anything, anywhere",
    author = "Krulvis",
    version = "1.2.0",
    markdownFileName = "Fighter.md",
    scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
    category = ScriptCategory.Combat
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            "Warrior guild", "Collect defenders in the warrior guild",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            "High alch", "High-alch if HA-value is at least 90% of GE-value",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
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
            "safespot", "Get safespot / centertile",
            optionType = OptionType.TILE
        ),
        ScriptConfiguration(
            "Loot price", "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
        ),
        ScriptConfiguration(
            "Always loot",
            "Separate items with \",\" Start with \"!\" to never loot",
            optionType = OptionType.STRING,
            defaultValue = "Long bone, curved bone, ensouled, rune, clue, !adamantite bar"
        ),
        ScriptConfiguration(
            "Bury bones", "Bury bones (put the nammes in `Always loot` field).",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            "bank", "Choose bank", optionType = OptionType.STRING, defaultValue = "FALADOR_WEST_BANK",
            allowedValues = ["NEAREST", "LUMBRIDGE_TOP", "FALADOR_WEST_BANK", "FALADOR_EAST_BANK", "LUMBRIDGE_CASTLE_BANK", "VARROCK_WEST_BANK", "VARROCK_EAST_BANK", "CASTLE_WARS_BANK", "EDGEVILLE_BANK", "DRAYNOR_BANK", "SEERS_BANK", "AL_KHARID_BANK", "SHANTAY_PASS_BANK", "CANIFIS_BANK", "CATHERBY_BANK", "YANILLE_BANK", "ARDOUGNE_NORTH_BANK", "ARDOUGNE_SOUTH_BANK", "MISCELLANIA_BANK", "GNOME_STRONGHOLD_BANK", "TZHAAR_BANK", "FISHING_GUILD_BANK", "BURTHORPE_BANK", "PORT_SARIM_DB", "MOTHERLOAD_MINE", "MINING_GUILD", "MOTHERLOAD_MINE_DEPOSIT", "FARMING_GUILD_85", "FARMING_GUILD_65", "WARRIORS_GUILD"]
        )
    ]
)
class Fighter : ATScript() {
    override fun createPainter(): ATPaint<*> = FighterPainter(this)
    override val rootComponent: TreeComponent<*> = ShouldEat(this)

    @ValueChanged("Warrior guild")
    fun onWGChange(inWG: Boolean) {
        if (inWG) {
            updateOption("safespot", warriorGuildCenter, OptionType.TILE)
            val npcAction = NpcActionEvent(
                0, 0, 10, 13729,
                "Attack", "<col=ffff00>Cyclops<col=40ff00>  (level-106)",
                447, 447
            )
            updateOption("monsters", listOf(npcAction), OptionType.NPC_ACTIONS)
            updateOption("radius", 25, OptionType.INTEGER)
            updateOption("bank", "WARRIORS_GUILD", OptionType.STRING)
        }
    }

    override fun onStart() {
        super.onStart()
        lastDefenderIndex = currentDefenderIndex()
    }

    val warriorGuildCenter = Tile(2859, 3545, 2)
    val warriorTokens = 8851
    val warriorGuild by lazy { getOption<Boolean>("Warrior guild")!! }
    val highAlch by lazy { getOption<Boolean>("High alch")!! }
    val useSafespot by lazy { getOption<Boolean>("Use safespot")!! }
    val safespot by lazy { getOption<Tile>("safespot")!! }
    val buryBones by lazy { getOption<Boolean>("Bury bones")!! }

    val defenders = listOf(8844, 8845, 8846, 8847, 8848, 8849, 8850, 12954)
    var lastDefenderIndex = -1
    fun currentDefenderIndex(): Int {
        val inv = Inventory.stream().list().map { it.id }
        val equipped = org.powbot.api.rt4.Equipment.itemAt(Slot.OFF_HAND).id
        val defender = defenders.lastOrNull { it in inv || equipped == it }
        return defenders.indexOf(defender)
    }

    val inventoryOptions by lazy { getOption<Map<Int, Int>>("inventory")!! }
    val inventory by lazy { inventoryOptions.filterNot { Potion.isPotion(it.key) } }
    val potions by lazy {
        inventoryOptions.filter { Potion.isPotion(it.key) }.mapNotNull { Pair(Potion.forId(it.key), it.value) }
            .groupBy {
                it.first
            }.map { it.key!! to it.value.sumOf { pair -> pair.second } }
    }
    val hasPrayPots by lazy { potions.any { it.first == Potion.PRAYER } }

    val equipmentOptions by lazy { getOption<Map<Int, Int>>("equipment")!! }
    val equipment by lazy {
        equipmentOptions.filterNot { TeleportItem.isTeleportItem(it.key) }.map {
            Equipment(
                emptyList(),
                Slot.forIndex(it.value),
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
    val lootNameOptions by lazy {
        val names = getOption<String>("Always loot")!!.split(",")
        val trimmed = mutableListOf<String>()
        names.forEach { trimmed.add(it.trim().lowercase()) }
        trimmed
    }

    val lootNames by lazy {
        val names = lootNameOptions.filterNot { it.startsWith("!") }.toMutableList()
        val ammo = equipment.firstOrNull { it.slot == Slot.QUIVER }
        if (ammo != null) {
            names.add(ItemLoader.load(ammo.id)?.name?.lowercase() ?: "nulll")
        }
        names.add("brimstone key")
        log.info("Looting: [${names.joinToString()}]")
        names.toList()
    }

    val neverLoot by lazy {
        val trimmed = mutableListOf<String>()
        lootNameOptions
            .filter { it.startsWith("!") }
            .forEach { trimmed.add(it.replace("!", "")) }
        log.info("Not looting: [${trimmed.joinToString()}]")
        trimmed
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

    fun canEat(extra: Int = 0) = food != null && ATContext.missingHP() > food!!.healing + extra

    fun needFood(): Boolean = ATContext.currentHP().toDouble() / ATContext.maxHP().toDouble() < .4


    fun getNearbyMonsters(): List<Npc> {
        return Npcs.stream().within(safespot, radius.toDouble()).name(*monsters.toTypedArray()).nearest().list()
    }

    fun target(): Npc? {
        val local = Players.local()
        val nearbyMonsters = getNearbyMonsters().filterNot { it.healthBarVisible() && it.interacting() != local }
        val attackingMe = nearbyMonsters.firstOrNull { it.interacting() == local }
        return attackingMe ?: nearbyMonsters.filter {
            !it.healthBarVisible() || it.healthPercent() > 0
        }.firstOrNull()
    }

    /**
     * Returns list of loot on the ground
     * WarriorsGuild: Defenders enabled by default
     * Neverloot: Needs to match name exactly (ignoreCasing=true) to skip item
     * LootNames: If [GroundItem] name contains one of lootNames, it will be accepted
     * Price: If price * stackSize >= minPrice
     *
     * @return [List] with [GroundItem]
     */
    fun loot(): List<GroundItem> {
        return GroundItems.stream()
            .within(if (useSafespot) safespot else Players.local().tile(), this.radius + 5.0)
            .filtered {
                if (warriorGuild && it.id() in defenders) return@filtered true
                val name = it.name().lowercase()
                !neverLoot.contains(name) &&
                        (lootNames.any { ln -> name.contains(ln) } || GrandExchange.getItemPrice(it.id()) * it.stackSize() >= minLoot)
            }.list()
    }

    @com.google.common.eventbus.Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        val id = evt.itemId
        val pot = Potion.forId(evt.itemId)
        val isTeleport = TeleportItem.isTeleportItem(id)
        if (evt.quantityChange > 0 && id != VIAL
            && id !in defenders
            && !inventory.containsKey(id) && !equipmentOptions.containsKey(id)
            && !isTeleport && potions.none { it.first == pot }
        ) {
            if (painter.paintBuilder.items.none { row -> row.any { it is InventoryItemPaintItem && it.itemId == id } }) {
                painter.paintBuilder.trackInventoryItems(id)
                log.info("Now tracking: ${ItemLoader.load(id)?.name} adding ${evt.quantityChange} as start")
                painter.paintBuilder.items.forEach { row ->
                    val item = row.firstOrNull { it is InventoryItemPaintItem && it.itemId == id }
                    if (item != null) (item as InventoryItemPaintItem).diff += evt.quantityChange
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
            .trackSkill(Skill.Slayer)
        return paintBuilder.build()
    }

    override fun paintCustom(g: Graphics) {
    }
}

fun main() {
    Fighter().startScript("127.0.0.1", "krullieman", false)
}