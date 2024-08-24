package org.powbot.krulvis.fighter

import com.google.common.eventbus.Subscribe
import org.powbot.api.Notifications
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Equipment.Slot
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.*
import org.powbot.api.script.paint.CheckboxPaintItem
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.dead
import org.powbot.krulvis.api.ATContext.getPrice
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.TargetWidget
import org.powbot.krulvis.api.extensions.Timer
import org.powbot.krulvis.api.extensions.items.Item.Companion.VIAL
import org.powbot.krulvis.api.extensions.items.Potion
import org.powbot.krulvis.api.extensions.items.TeleportEquipment
import org.powbot.krulvis.api.extensions.requirements.EquipmentRequirement
import org.powbot.krulvis.api.extensions.requirements.InventoryRequirement
import org.powbot.krulvis.api.extensions.teleports.*
import org.powbot.krulvis.api.extensions.teleports.poh.LUNAR_ISLE_HOUSE_PORTAL
import org.powbot.krulvis.api.extensions.teleports.poh.openable.CASTLE_WARS_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.teleports.poh.openable.EDGEVILLE_MOUNTED_GLORY
import org.powbot.krulvis.api.extensions.teleports.poh.openable.FEROX_ENCLAVE_JEWELLERY_BOX
import org.powbot.krulvis.api.extensions.watcher.LootWatcher
import org.powbot.krulvis.api.extensions.watcher.NpcDeathWatcher
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.script.tree.branch.ShouldConsume
import org.powbot.krulvis.api.script.tree.branch.ShouldSipPotion
import org.powbot.krulvis.fighter.Defender.currentDefenderIndex
import org.powbot.krulvis.fighter.tree.branch.ShouldStop
import org.powbot.mobile.script.ScriptManager
import kotlin.math.floor
import kotlin.random.Random


//<editor-fold desc="ScriptManifest">
@ScriptManifest(
    name = "krul Fighter",
    description = "Fights anything, anywhere. Supports defender collecting.",
    author = "Krulvis",
    version = "1.5.6",
    markdownFileName = "Fighter.md",
    scriptId = "d3bb468d-a7d8-4b78-b98f-773a403d7f6d",
    category = ScriptCategory.Combat,
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            WARRIOR_GUILD_OPTION, "Collect defenders in the warrior guild",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            INVENTORY_OPTION, "What should your inventory look like?",
            optionType = OptionType.INVENTORY
        ),
        ScriptConfiguration(
            PRAY_AT_ALTAR_OPTION, "Pray at nearby altar.",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            EQUIPMENT_OPTION, "What gear do you want to use?",
            optionType = OptionType.EQUIPMENT, visible = true
        ),
        ScriptConfiguration(
            MONSTERS_OPTION,
            "Click the NPC's you want to kill",
            optionType = OptionType.NPC_ACTIONS
        ),
        ScriptConfiguration(
            MONSTER_AUTO_DESTROY_OPTION, "Select if monster is finished off (Gargoyles)",
            OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            RADIUS_OPTION, "Kill radius", optionType = OptionType.INTEGER, defaultValue = "10"
        ),
        ScriptConfiguration(
            HOP_FROM_PLAYERS_OPTION, "Do you want to hop from players?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            PLAYER_HOP_COUNT_OPTION, "At how many players in radius do you want to hop?",
            optionType = OptionType.INTEGER, defaultValue = "1", visible = false
        ),
        ScriptConfiguration(
            USE_SAFESPOT_OPTION, "Do you want to force a safespot?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            SAFESPOT_RADIUS, "Safespot allowed radius",
            optionType = OptionType.INTEGER, defaultValue = "0", visible = false
        ),
        ScriptConfiguration(
            WALK_BACK_TO_SAFESPOT_OPTION,
            "Walk to safespot after attacking?",
            optionType = OptionType.BOOLEAN,
            defaultValue = "false",
            visible = false
        ),
        ScriptConfiguration(
            CENTER_TILE_OPTION, "Get safespot/center tile",
            optionType = OptionType.TILE
        ),
        ScriptConfiguration(
            WAIT_FOR_LOOT_OPTION, "Wait for loot after kill?",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),
        ScriptConfiguration(
            IRONMAN_DROPS_OPTION, description = "Only pick up your own drops.",
            optionType = OptionType.BOOLEAN, defaultValue = "true"
        ),
        ScriptConfiguration(
            LOOT_PRICE_OPTION, "Min loot price?", optionType = OptionType.INTEGER, defaultValue = "1000"
        ),
        ScriptConfiguration(
            LOOT_OVERRIDES_OPTION,
            "Separate items with \",\" Start with \"!\" to never loot",
            optionType = OptionType.STRING,
            defaultValue = "Long bone, curved bone, clue, totem, !blue dragon scale, Scaly blue dragonhide, toadflax, irit, avantoe, kwuarm, snapdragon, cadantine, lantadyme, dwarf weed, torstol"
        ),
        ScriptConfiguration(
            BURY_BONES_OPTION, "Bury, Scatter or Offer bones&ashes.",
            optionType = OptionType.BOOLEAN, defaultValue = "false"
        ),

        ScriptConfiguration(USE_CANNON_OPTION, "Use a cannon?", OptionType.BOOLEAN, "false"),
        ScriptConfiguration(CANNON_TILE_OPTION, "Where to place cannon?", OptionType.TILE, visible = false),
        ScriptConfiguration(
            BANK_TELEPORT_OPTION,
            "Teleport to bank",
            optionType = OptionType.STRING,
            defaultValue = EDGEVILLE_MOUNTED_GLORY,
            allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX]
        ),
        ScriptConfiguration(
            MONSTER_TELEPORT_OPTION,
            "Teleport to Monsters",
            optionType = OptionType.STRING,
            defaultValue = "NONE",
            allowedValues = ["NONE", EDGEVILLE_GLORY, EDGEVILLE_MOUNTED_GLORY, FEROX_ENCLAVE_ROD, FEROX_ENCLAVE_JEWELLERY_BOX, CASTLE_WARS_ROD, CASTLE_WARS_JEWELLERY_BOX, LUNAR_ISLE_HOUSE_PORTAL]
        )
    ]
)
//</editor-fold>
class Fighter : ATScript() {

    override fun createPainter(): ATPaint<*> = FighterPainter(this)

    override val rootComponent: TreeComponent<*> = ShouldConsume(this, ShouldStop(this))
    override fun onStart() {
        super.onStart()
        Defender.lastDefenderIndex = currentDefenderIndex()
        if (prayAtNearbyAltar) {
            ShouldSipPotion.skippingPotions.addAll(listOf(Potion.PRAYER, Potion.SUPER_RESTORE))
        }
    }

    //<editor-fold desc="UISubscribers">
    @ValueChanged(WARRIOR_GUILD_OPTION)
    fun onWGChange(inWG: Boolean) {
        if (inWG) {
            updateOption(USE_SAFESPOT_OPTION, false, OptionType.BOOLEAN)
            updateOption(CENTER_TILE_OPTION, Defender.killSpot(), OptionType.TILE)
            val npcAction = NpcActionEvent(
                0, 0, 10, 13729, 0,
                "Attack", "<col=ffff00>Cyclops<col=40ff00>  (level-106)",
                447, 447, -1
            )
            updateOption(MONSTERS_OPTION, listOf(npcAction), OptionType.NPC_ACTIONS)
            updateOption(RADIUS_OPTION, 25, OptionType.INTEGER)
        }
    }

    @ValueChanged(USE_CANNON_OPTION)
    fun onUseCannon(useCannon: Boolean) {
        updateVisibility(CANNON_TILE_OPTION, useCannon)
    }


    @ValueChanged(USE_SAFESPOT_OPTION)
    fun onSafeSpotChange(useSafespot: Boolean) {
        updateVisibility(WALK_BACK_TO_SAFESPOT_OPTION, useSafespot)
        updateVisibility(SAFESPOT_RADIUS, useSafespot)
    }

    @ValueChanged(HOP_FROM_PLAYERS_OPTION)
    fun onHopFromPlayersChange(hopFromPlayers: Boolean) {
        updateVisibility(PLAYER_HOP_COUNT_OPTION, hopFromPlayers)
    }


    //</editor-fold desc="UISubscribers">


    //Warrior guild
    val warriorTokens = 8851
    val warriorGuild by lazy { getOption<Boolean>(WARRIOR_GUILD_OPTION) }

    //Inventory
    private val inventoryOptions by lazy { getOption<Map<Int, Int>>(INVENTORY_OPTION) }
    val requiredInventory by lazy { inventoryOptions.map { InventoryRequirement(it.key, it.value) } }
//	val requiredPotions by lazy {
//		requiredInventory.filter { it.item is Potion }.map { PotionRequirement(it.item as Potion, it.amount) }
//	}

    //Equipment
    fun getEquipment(optionKey: String): List<EquipmentRequirement> {
        val option = getOption<Map<Int, Int>>(optionKey)
        return option.map {
            EquipmentRequirement(
                it.key,
                Slot.forIndex(it.value)!!,
            )
        }
    }

    val equipment by lazy { getEquipment(EQUIPMENT_OPTION) }

    val ammo by lazy {
        equipment.firstOrNull { it.slot == Slot.QUIVER }
    }
    val ammoId by lazy { ammo?.item?.id ?: -1 }

    val teleportEquipments by lazy {
        equipment.mapNotNull { TeleportEquipment.getTeleportItem(it.item.id) }
    }

    //Loot
    fun isLootWatcherActive() = lootWachter?.active == true
    var lootWachter: LootWatcher? = null
    var kills = 0
    val lootList = mutableListOf<GroundItem>()
    val ironman by lazy { getOption<Boolean>(IRONMAN_DROPS_OPTION) }
    val waitForLootAfterKill by lazy { getOption<Boolean>(WAIT_FOR_LOOT_OPTION) }
    val minLootPrice by lazy { getOption<Int>(LOOT_PRICE_OPTION) }
    val lootNameOptions by lazy {
        val names = getOption<String>(LOOT_OVERRIDES_OPTION).split(",")
        val trimmed = mutableListOf<String>()
        names.forEach { trimmed.add(it.trim().lowercase()) }
        trimmed
    }
    val lootNames by lazy {
        val names = lootNameOptions.filterNot { it.startsWith("!") }.toMutableList()
        if (ammo != null) {
            names.add(ammo!!.item.name)
        }
        names.add("hydra's")
        names.add("visage")
        names.add("brimstone key")
        names.add("ancient shard")
        names.add("basilisk jaw")
        logger.info("Looting: [${names.joinToString()}]")
        names.toList()
    }
    val neverLoot by lazy {
        val trimmed = mutableListOf<String>()
        lootNameOptions
            .filter { it.startsWith("!") }
            .forEach { trimmed.add(it.replace("!", "")) }
        logger.info("Not looting: [${trimmed.joinToString()}]")
        trimmed
    }

    fun watchLootDrop(tile: Tile) {
        if (!isLootWatcherActive()) {
            logger.info("Waiting for loot at $tile")
            lootWachter = LootWatcher(tile, intArrayOf(ammoId), lootList = lootList, isLoot = { it.isLoot() })
        } else {
            logger.info("Already watching loot at tile: $tile for loot")
        }
    }

    fun GroundItem.isLoot() = isLoot(stackSize())

    private fun GenericItem.isLoot(amount: Int): Boolean {
        if (warriorGuild && id() in Defender.defenders) return true
        val nameLC = name().lowercase()
        return !neverLoot.contains(nameLC) &&
                (lootNames.any { ln -> nameLC.contains(ln) } || getPrice() * amount >= minLootPrice)
    }


    fun loot(): List<GroundItem> =
        if (ironman) lootList else GroundItems.stream().within(centerTile, killRadius).filter { it.isLoot() }

    var npcDeathWatchers: MutableList<NpcDeathWatcher> = mutableListOf()

    //Banking option
    var forcedBanking = false
    var lastTrip = false
    val bankTeleport by lazy { TeleportMethod(Teleport.forName(getOption(BANK_TELEPORT_OPTION))) }


    //monsters Killing spot
    private val monsters by lazy {
        getOption<List<NpcActionEvent>>(MONSTERS_OPTION).map { it.name }
    }
    val autoDestroyMonster by lazy { getOption<Boolean>(MONSTER_AUTO_DESTROY_OPTION) }
    val monsterTeleport by lazy { TeleportMethod(Teleport.forName(getOption(MONSTER_TELEPORT_OPTION))) }
    var currentTarget: Npc = Npc.Nil
    val aggressionTimer = Timer(15 * 60 * 1000)

    private val monsterNames: List<String> get() = if (superiorAppeared) SUPERIORS else monsters
    fun nearbyMonsters(): List<Npc> =
        Npcs.stream().within(centerTile, killRadius.toDouble()).name(*monsterNames.toTypedArray()).nearest().list()

    private fun Npc.attackingOtherPlayer(): Boolean {
        val interacting = interacting()
        return interacting is Player && interacting != Players.local()
    }

//	private val GWD_AREA = Area(Tile(2816, 5120), Tile(3008, 5376))

    fun target(): Npc {
        val local = Players.local()
        val nearbyMonsters =
            nearbyMonsters().filterNot { it.healthBarVisible() && (it.attackingOtherPlayer() || it.healthPercent() == 0) }
                .sortedBy { it.distance() }
        val attackingMe =
            nearbyMonsters.firstOrNull { it.interacting() is Npc || it.interacting() == local && it.reachable() }
        return attackingMe ?: nearbyMonsters.firstOrNull { it.reachable() } ?: Npc.Nil
    }

    //Safespot options
    val killRadius by lazy { getOption<Int>(RADIUS_OPTION) }
    val safespotRadius by lazy { getOption<Int>(SAFESPOT_RADIUS) }
    val useSafespot by lazy { getOption<Boolean>(USE_SAFESPOT_OPTION) }
    val walkBack by lazy { getOption<Boolean>(WALK_BACK_TO_SAFESPOT_OPTION) }
    val centerTile by lazy {
        val tile = getOption<Tile>(CENTER_TILE_OPTION)
        if (tile == Tile.Nil) {
            Notifications.showNotification("Using current tile as you forgot to set a tile")
            return@lazy Players.local().tile()
        }
        return@lazy tile
    }
    val buryBones by lazy { getOption<Boolean>(BURY_BONES_OPTION) }
    fun shouldReturnToSafespot() = useSafespot
            && centerTile.distance() > safespotRadius
            && (walkBack || Players.local().healthBarVisible())
            && projectiles.none { it.first.destination() == centerTile }

    //Hop from players options
    val hopFromPlayers by lazy { getOption<Boolean>(HOP_FROM_PLAYERS_OPTION) }
    val playerHopAmount by lazy { getOption<Int>(PLAYER_HOP_COUNT_OPTION) }

    //Prayer options
    val prayAtNearbyAltar by lazy { getOption<Boolean>(PRAY_AT_ALTAR_OPTION) }
    var nextAltarPrayRestore = Random.nextInt(5, 15)

    private val usingPrayer by lazy {
        prayAtNearbyAltar || requiredInventory.filter { it.item is Potion }
            .any { (it.item as Potion).skill == Constants.SKILLS_PRAYER }
    }

    fun canActivateQuickPrayer() = usingPrayer && !Prayer.quickPrayer() && Prayer.prayerPoints() > 0
    fun canDeactivateQuickPrayer() = Prayer.quickPrayer() &&
            aggressionTimer.isFinished() && useSafespot && Npcs.stream().interactingWithMe().none { !it.dead() }

    //Cannon option
    val useCannon by lazy { getOption<Boolean>(USE_CANNON_OPTION) }
    val cannonTile by lazy { getOption<Tile>(CANNON_TILE_OPTION) }
    fun getCannon() =
        Objects.stream(cannonTile, GameObject.Type.INTERACTIVE).name("Broken multicannon", "Dwarf multicannon").first()

    //Custom slayer options
    var lastTask = false
    var superiorAppeared = false
    private val slayerBraceletNames = arrayOf("Bracelet of slaughter", "Expeditious bracelet")
    fun getSlayerBracelet() = Inventory.stream().name(*slayerBraceletNames).first()
    fun wearingSlayerBracelet() = Equipment.stream().name(*slayerBraceletNames).isNotEmpty()
    val hasSlayerBracelet by lazy { getSlayerBracelet().valid() }


    @Subscribe
    fun onTickEvent(_e: TickEvent) {
        if (ScriptManager.state() != ScriptState.Running) return
        val time = System.currentTimeMillis()
        projectiles.forEach {
            if (time - it.second > projectileDuration) {
                projectiles.remove(it)
            }
        }
        val interacting = me.interacting()
        if (interacting is Npc && interacting != Npc.Nil) {
            currentTarget = interacting
            val activeLW = lootWachter
            if (activeLW?.active == true && activeLW.tile.distanceTo(currentTarget.tile()) < 2) return
            val deathWatcher = npcDeathWatchers.firstOrNull { it.npc == interacting }
            if (deathWatcher == null || !deathWatcher.active) {
                val newDW = NpcDeathWatcher(
                    interacting,
                    autoDestroyMonster
                ) {
                    kills++
                    if (hasSlayerBracelet && !wearingSlayerBracelet()) {
                        val slayBracelet = getSlayerBracelet()
                        if (slayBracelet.valid()) {
                            getSlayerBracelet().fclick()
                            logger.info("Wearing bracelet on death at ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
                        }
                    }
                    if (ironman) {
                        watchLootDrop(interacting.tile())
                    }
                    if (interacting.name.lowercase() in SUPERIORS) {
                        superiorAppeared = false
                    }
                }
                npcDeathWatchers.add(newDW)
            }
        }
        npcDeathWatchers.removeAll { !it.active }
    }

    @Subscribe
    fun onInventoryChange(evt: InventoryChangeEvent) {
        if (ScriptManager.state() != ScriptState.Running) return
        val id = evt.itemId
        val isTeleport = TeleportEquipment.isTeleportItem(id)
        if (evt.quantityChange > 0 && id != VIAL
            && id !in Defender.defenders
            && requiredInventory.none { id in it.item.ids }
            && equipment.none { it.item.ids.contains(id) }
            && !isTeleport
        ) {
            painter.trackItem(id, evt.quantityChange)
        }
    }

    @Subscribe
    fun experienceEvent(xpEvent: SkillExpGainedEvent) {
        if (xpEvent.skill == Skill.Slayer) {
            logger.info("Slayer xp at: ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
        } else if (xpEvent.skill == Skill.Hitpoints) {
            val dmg = floor(xpEvent.expGained / 1.33).toInt()
            if (TargetWidget.health() - dmg <= 0 && hasSlayerBracelet && !wearingSlayerBracelet()) {
                val slayBracelet = getSlayerBracelet()
                if (slayBracelet.valid()) {
                    logger.info("Wearing bracelet on xp drop ${System.currentTimeMillis()}, cycle=${Game.cycle()}")
                    slayBracelet.fclick()
                }
            }
        }
    }

    var projectiles = mutableListOf<Pair<Projectile, Long>>()
    var projectileSafespot = Tile.Nil
    val projectileDuration = 2400
    var fightingFromDistance = false

    private fun findSafeSpotFromProjectile() {
        val dangerousTiles = projectiles.map { it.first.destination() }
        val myTile = me.tile()
        val targTile = currentTarget.tile()
        val myDistance = targTile.distance()
        val collisionMap = Movement.collisionMap(myTile.floor).collisionMap.flags
        val grid = mutableListOf<Pair<Tile, Double>>()
        for (x in -2 until 2) {
            for (y in -2 until 2) {
                val t = Tile(myTile.x + x, myTile.y + y, myTile.floor)
                if (!t.blocked(collisionMap)) {
                    grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
                }
            }
        }
        projectileSafespot =
            grid.filter { it.first.distanceTo(targTile) > myDistance }.maxByOrNull { it.second }!!.first
    }

    @Subscribe
    fun onProjectile(e: ProjectileDestinationChangedEvent) {
        if (e.target() == Actor.Nil) {
            val distance = e.destination().distance()
            if (distance < 2) {
                logger.info("Dangerous projectile spawned! tile=${e.destination()}")
                projectiles.add(e.projectile to System.currentTimeMillis())
                findSafeSpotFromProjectile()
                if (distance == 0.0) {
                    Movement.step(projectileSafespot, 0)
                }
            }
        } else if (e.target() == currentTarget) {
            fightingFromDistance = true
        }
    }

    @Subscribe
    fun messageReceived(msg: MessageEvent) {
        if (msg.messageType != MessageType.Game) return
        if (msg.message.contains("so you can't take ")) {
            logger.info("Ironman message CANT TAKE type=${msg.messageType}")
            lootList.clear()
        }
        if (msg.message.contains("A superior foe has appeared")) {
            logger.info("Superior appeared message received: type=${msg.messageType}")
            superiorAppeared = true
        }
    }

    @Subscribe
    fun onPaintCheckbox(pcce: PaintCheckboxChangedEvent) {
        if (pcce.checkboxId == "stopAfterTask") {
            lastTask = pcce.checked
            val painter = painter as FighterPainter
            if (pcce.checked && !painter.paintBuilder.items.contains(painter.slayerTracker)) {
                val index =
                    painter.paintBuilder.items.indexOfFirst { row -> row.any { it is CheckboxPaintItem && it.id == "stopAfterTask" } }
                painter.paintBuilder.items.add(index, painter.slayerTracker)
            } else if (!pcce.checked && painter.paintBuilder.items.contains(painter.slayerTracker)) {
                painter.paintBuilder.items.remove(painter.slayerTracker)
            }
        } else if (pcce.checkboxId == "stopAtBank") {
            lastTrip = pcce.checked
        }
    }
}


fun main() {
    Fighter().startScript("127.0.0.1", "GIM", false)
}