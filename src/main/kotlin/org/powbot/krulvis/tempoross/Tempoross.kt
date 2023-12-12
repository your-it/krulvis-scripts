package org.powbot.krulvis.tempoross

import com.google.common.eventbus.Subscribe
import org.powbot.api.InteractableEntity
import org.powbot.api.Locatable
import org.powbot.api.Tile
import org.powbot.api.event.BreakEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.PaintCheckboxChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPaint
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.PARENT_WIDGET
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Data.WAVE_TIMER
import org.powbot.krulvis.tempoross.tree.branch.ShouldEnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.EnterBoat
import org.powbot.krulvis.tempoross.tree.leaf.Leave

@ScriptManifest(
        name = "krul Tempoross",
        description = "Does tempoross minigame",
        version = "1.2.9",
        author = "Krulvis",
        markdownFileName = "Tempoross.md",
        category = ScriptCategory.Fishing
)
@ScriptConfiguration.List(
        [
            ScriptConfiguration(
                    name = UI.EQUIPMENT,
                    description = "What equipment to wear",
                    defaultValue = """{"25592":0,"21028":3,"25594":4,"25596":7,"25598":10,"2554":12}""",
                    optionType = OptionType.EQUIPMENT
            ),
            ScriptConfiguration(
                    name = UI.BUCKETS,
                    description = "How many buckets to take?",
                    defaultValue = "5",
                    optionType = OptionType.INTEGER
            ),
            ScriptConfiguration(
                    name = UI.COOK_FISH,
                    description = "Cooking the fish gives more points at the cost of XP",
                    defaultValue = "true",
                    optionType = OptionType.BOOLEAN
            ),
            ScriptConfiguration(
                    name = UI.SPECIAL_ATTACK,
                    description = "Do Dragon/Inferno Harpoon special",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            ),
            ScriptConfiguration(
                    name = UI.BARB_FISHING,
                    description = "Barbarian fishing",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            ),
            ScriptConfiguration(
                    name = UI.IMCANDO_HAMMER,
                    description = "Take Imcando hammer",
                    defaultValue = "false",
                    optionType = OptionType.BOOLEAN
            )
        ]
)
class Tempoross : ATScript() {
    override val rootComponent: TreeComponent<*> = ShouldEnterBoat(this)

    override fun createPainter(): ATPaint<*> {
        return TemporossPaint(this)
    }

    val waveTimer = Timer(0)
    var side = Side.UNKNOWN
    var forcedShooting = false
    val burningTiles = mutableListOf<Tile>()
    val triedPaths = mutableListOf<LocalPath>()
    var rewardGained = 0
    var pointsObtained = 0
    var rounds = 0
    var lastGame = false
    var debugPaint = false
    var bestFishSpot: Npc? = null
    var fishSpots: List<Pair<Npc, LocalPath>> = emptyList()
    val hasOutfit by lazy { intArrayOf(25592, 25594, 25596, 25598).all { it in equipment.keys } }

    val barbFishing by lazy { getOption<Boolean>(UI.BARB_FISHING) }
    val cookFish by lazy { getOption<Boolean>(UI.COOK_FISH) }
    val spec by lazy { getOption<Boolean>(UI.SPECIAL_ATTACK) }
    val buckets by lazy { getOption<Int>(UI.BUCKETS) }
    val equipment by lazy { getOption<Map<Int, Int>>(UI.EQUIPMENT) }

    fun hasDangerousPath(end: Tile): Boolean {
        val path = LocalPathFinder.findPath(end)
        return containsDangerousTile(path)
    }

    fun containsDangerousTile(path: LocalPath): Boolean {
        triedPaths.add(path)
        return path.actions.any { burningTiles.contains(it.destination) }
    }

    fun interactWhileDousing(
            e: InteractableEntity?,
            action: String,
            destinationWhenNil: Tile,
            allowCrossing: Boolean
    ): Boolean {
        if (e == null) {
            log.info("Can't find: $action")
            if (destinationWhenNil != Tile.Nil) {
                val path = LocalPathFinder.findPath(destinationWhenNil)
                if (path.isNotEmpty() && douseIfNecessary(path, allowCrossing)) {
                    walkPath(path)
                } else {
                    log.info(if (path.isEmpty()) "Path is empty" else "failed dousing")
                    walk(destinationWhenNil)
                }
            }
        } else {
            var path = LocalPathFinder.findPath(e.tile())
            if (path.isEmpty()) path = LocalPathFinder.findPath(destinationWhenNil)
            if (douseIfNecessary(path, allowCrossing)) {
                return walkAndInteract(e, action)
            }
        }
        return false
    }

    fun walkWhileDousing(tile: Tile, allowCrossing: Boolean): Boolean {
        return walkWhileDousing(LocalPathFinder.findPath(tile), allowCrossing)
    }

    fun walkWhileDousing(path: LocalPath, allowCrossing: Boolean): Boolean {
        if (douseIfNecessary(path, allowCrossing)) {
            return walkPath(path)
        }
        return true
    }


    private fun douseIfNecessary(path: LocalPath, allowCrossing: Boolean = false): Boolean {
        val blockedTile = path.actions.firstOrNull { burningTiles.contains(it.destination) }
        val fire =
                if (blockedTile != null) Npcs.stream().name("Fire").nearest(blockedTile.destination)
                        .firstOrNull() else null
        val hasBucket = Inventory.containsOneOf(BUCKET_OF_WATER)
        log.info("Blockedtile: $blockedTile fire: $fire, Bucket: $hasBucket")
        if (fire != null && hasBucket) {
            log.info("Found fire on the way to: ${path.finalDestination()}")
            if (!fire.inViewport()) {
                val blockedIndex = path.actions.indexOf(blockedTile)
                if (fire.distance() > 8 && blockedIndex >= 2) {
                    val safeSpot = path.actions[blockedIndex - 2].destination
                    Movement.step(safeSpot)
                }
                Camera.turnTo(fire)
                waitFor(long()) { fire.inViewport() }
            } else if (fire.interact("Douse")) {
                return waitFor(long()) { Npcs.stream().at(fire).name("Fire").isEmpty() }
            }
        } else if (fire == null || allowCrossing) {
            log.info(if (fire == null) "No fire on the way" else "allowCrossing=true")
            return true
        }
        return false
    }

    private fun walkPath(path: LocalPath): Boolean {
        if (path.isEmpty()) {
            return false
        }

        val finalTile = path.actions.last().destination
        return if (finalTile.matrix().onMap() && finalTile.distance() > 5) {
            Movement.step(finalTile)
        } else if (path.isNotEmpty()) {
            path.traverse()
        } else {
            false
        }
    }

    fun canKill(): Boolean = getEnergy() in 0..2 || getBossPool() != null

    fun isTethering(): Boolean = (Varpbits.varpbit(2933) xor 7 and 7) != 7

    fun getEnergy(): Int {
        val text = Widgets.widget(PARENT_WIDGET).firstOrNull {
            it != null && it.visible() && it.text().contains("Energy")
        }?.text() ?: return -1
        return text.substring(8, text.indexOf("%")).toInt()
    }

    fun getHealth(): Int {
        val text = Widgets.widget(PARENT_WIDGET).firstOrNull {
            it != null && it.visible() && it.text().contains("Essence")
        }?.text() ?: return -1
        return text.substring(9, text.indexOf("%")).toInt()
    }

    override fun canBreak(): Boolean {
        val canBreak = lastLeaf is EnterBoat || lastLeaf is Leave
        log.info("canBreak() lastLeaf=${lastLeaf.name} canBreak=${canBreak}")
        return canBreak
    }

    @Subscribe
    fun onBreakEvent(be: BreakEvent) {
        if (!canBreak()) {
            be.delay(1000)
        }
    }

    @Subscribe
    fun onMessageEvent(me: MessageEvent?) {
        if (me == null || me.type != 0) {
            return
        }
        val txt = me.message
        log.info(txt)
        if (txt.contains("Points: ") && txt.contains("Personal best", true)) {
            val points = txt.substring(20, txt.indexOf("</col>")).replace(",", "").toInt()
            log.info("Finished round, gained: $points points")
            pointsObtained += points
            rounds++
        } else if (txt.contains("A colossal wave closes in...")) {
            log.info("Should tether wave coming in!")
            waveTimer.reset(WAVE_TIMER)
            val fishId = if (cookFish) COOKED else RAW
            val fish = Inventory.stream().id(fishId).count()
            if (fish >= 15 || fish >= getHealth()) {
                forcedShooting = true
            }
        } else if (txt.contains("Reward permits: ") && txt.contains("Total permits:")) {
            val reward = txt.substring(28, txt.indexOf("</col>")).toInt()
            log.info("Gained $reward points")
            rewardGained += reward
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onCheckBoxEvent(e: PaintCheckboxChangedEvent) {
        if (e.checkboxId == "lastGame") {
            lastGame = e.checked
        } else if (e.checkboxId == "paintDebug") {
            debugPaint = e.checked
        }
    }


    private fun Npc.rightSide(): Boolean {
        val y = tile().y
        return if (side == Side.NORTH) {
            y > side.mastLocation.y()
        } else {
            y < side.mastLocation.y()
        }
    }

    /**
     * Detect and add blocked tiles.
     */
    fun detectDangerousTiles() {
        Npcs.stream().filtered { it.animation() > 0 }.name("Lightning cloud").nearest().forEach {
            log.info("Found lightning cloud at ${it.tile()}")
            addTile(it.tile())
        }

        val fires = Npcs.stream().name("Fire")
        fires.forEach { fire ->
            log.info("Found fire at ${fire.tile()}")
            addTile(fire.tile())
        }
    }

    fun addTile(tile: Tile) {
        burningTiles.add(tile)
        burningTiles.add(Tile(tile.x(), tile.y() - 1, 0))
        burningTiles.add(Tile(tile.x() - 1, tile.y(), 0))
        burningTiles.add(Tile(tile.x() - 1, tile.y() - 1, 0))
    }

    fun collectFishSpots() {
        fishSpots = Npcs.stream().filtered { it.rightSide() }
                .action("Harpoon").name("Fishing spot").list()
                .map { Pair(it, LocalPathFinder.findPath(it.tile().getWalkableNeighbor())) }
    }

    fun getFishSpot(spots: List<Pair<Npc, LocalPath>>): Npc? {
        val paths = spots.filter { !containsDangerousTile(it.second) }
        val doublePath = paths.firstOrNull { it.first.id() == DOUBLE_FISH_ID }
        if (doublePath != null) {
            return doublePath.first
        }

        if (paths.isNotEmpty()) {
            return paths.minByOrNull { it.second.actions.size }!!.first
        }
        return null
    }

    fun getBossPool() =
            Npcs.stream().at(side.bossPoolLocation).action("Harpoon").name("Spirit pool").firstOrNull()

    fun getAmmoCrate(): Npc? =
            Npcs.stream().name("Ammunition crate").firstOrNull { it.atCorrectSide() }

    fun hasHammer() = Inventory.containsOneOf(Item.HAMMER, Item.IMCANDO_HAMMER)
    fun getHammerContainer(): GameObject? = Objects.stream()
            .type(GameObject.Type.INTERACTIVE).name("Hammers")
            .firstOrNull { it.atCorrectSide() }

    fun getBucketCrate(): GameObject? =
            Objects.stream(50).type(GameObject.Type.INTERACTIVE).filtered {
                it.atCorrectSide()
            }.name("Buckets").nearest().firstOrNull()

    fun getRopeContainer(): GameObject? = Objects.stream(50)
            .type(GameObject.Type.INTERACTIVE)
            .name("Ropes")
            .firstOrNull { it.atCorrectSide(6) }

    fun getWaterpump(): GameObject? =
            Objects.stream(50).type(GameObject.Type.INTERACTIVE).filtered {
                if (getEnergy() == -1) {
                    it.distance() <= 10
                } else {
                    it.atCorrectSide()
                }
            }.name("Water pump").nearest().firstOrNull()

    private fun Locatable.atCorrectSide(distance: Int = 5): Boolean {
        val tile = tile()
        return tile.distanceTo(side.mastLocation) <= distance || tile.distanceTo(side.bossPoolLocation) <= distance
    }

    fun getTetherPole(): GameObject? {
        val dest = Movement.destination()
        val destination = if (dest != Tile.Nil) dest else me.tile()
        val validTiles = listOf(side.totemLocation, side.mastLocation)
        return Objects.stream(50).type(GameObject.Type.INTERACTIVE).filtered {
            validTiles.contains(it.tile())
        }.action("Repair", "Tether").nearest(destination).firstOrNull()
    }

    fun getLadder(): GameObject? = Objects.stream().name("Rope ladder").action("Climb").firstOrNull()

    fun getBucketCount(): Int = Inventory.stream().name("Bucket").count().toInt()
}

fun main() {
    Tempoross().startScript("127.0.0.1", "GIM", false)
}
