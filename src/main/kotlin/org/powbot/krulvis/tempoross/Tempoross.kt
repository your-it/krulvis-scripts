package org.powbot.krulvis.tempoross

import com.google.common.eventbus.Subscribe
import org.powbot.api.InteractableEntity
import org.powbot.api.Tile
import org.powbot.api.event.MessageEvent
import org.powbot.api.event.PaintCheckboxChangedEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walk
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
    version = "1.2.3",
    author = "Krulvis",
    markdownFileName = "Tempoross.md",
    category = ScriptCategory.Fishing
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = "Cook fish",
            description = "Cooking the fish gives more points at the cost of XP",
            defaultValue = "true",
            optionType = OptionType.BOOLEAN
        ),
        ScriptConfiguration(
            name = "Special Attack",
            description = "Do Dragon Harpoon special",
            defaultValue = "false",
            optionType = OptionType.BOOLEAN
        ),
        ScriptConfiguration(
                name = "Barb fishing",
                description = "Barbarian fishing",
                defaultValue = "false",
                optionType = OptionType.BOOLEAN
        ),
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
    var profile = TemporossProfile()
    var rewardGained = 0
    var pointsObtained = 0
    var rounds = 0
    var lastGame = false
    var debugPaint = false
    var bestFishSpot: Npc? = null
    var fishSpots: List<Pair<Npc, LocalPath>> = emptyList()
    val hasOutfit by lazy { Equipment.stream().id(25592, 25594, 25596, 25598).count().toInt() == 4 }

    val barbFishing by lazy { getOption<Boolean>("Barb fishing") }
    val cookFish by lazy { getOption<Boolean>("Cook fish") }
    val spec by lazy { getOption<Boolean>("Special Attack") }

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
                    walk(destinationWhenNil)
                }
            }
        } else {
            var path = LocalPathFinder.findPath(e.tile())
            if (path.isEmpty()) path = LocalPathFinder.findPath(destinationWhenNil)
            if (douseIfNecessary(path, allowCrossing)) {
                return walkAndInteract(e as Interactive, action)
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

    /**
     * @param allowCrossing
     */
    fun douseIfNecessary(path: LocalPath, allowCrossing: Boolean = false): Boolean {
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
            log.info("No fire on the way")
            return true
        }
        return false
    }

    fun walkPath(path: LocalPath): Boolean {
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

    override fun canBreak(): Boolean {
        return lastLeaf is EnterBoat || lastLeaf is Leave
    }

    fun canKill(): Boolean = getEnergy() in 0..2 || getBossPool() != null

    fun isTethering(): Boolean = (Varpbits.varpbit(2933) xor 7 and 7) != 7

    fun getEnergy(): Int {
        val text =
            Widgets.widget(PARENT_WIDGET).firstOrNull { it != null && it.visible() && it.text().contains("Energy") }
                ?.text()
                ?: return -1
        return text.substring(8, text.indexOf("%")).toInt()
    }

    fun getHealth(): Int {
        val text =
            Widgets.widget(PARENT_WIDGET).firstOrNull { it != null && it.visible() && it.text().contains("Essence") }
                ?.text()
                ?: return -1
        return text.substring(9, text.indexOf("%")).toInt()
    }

    @Subscribe
    fun onMessageEvent(me: MessageEvent?) {
        val me = me ?: return
        if (me.type != 0) {
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
            if (profile.shootAfterTethering && (fish >= profile.minFishToForceShoot || fish >= getHealth())) {
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
        }else if(e.checkboxId == "paintDebug"){
            debugPaint = e.checked
        }
    }


    fun rightSide(spot: Npc): Boolean {
        return if (side == Side.NORTH) {
            spot.tile().y() > side.mastLocation.y()
        } else {
            spot.tile().y() < side.mastLocation.y()
        }
    }

    /**
     * Detect and add blocked tiles.
     */
    fun detectDangerousTiles() {
        Npcs.stream().filtered { it.animation() > 0 }.name("Lightning cloud").nearest().forEach {
            addTile(it.tile())
        }

        val fires = Npcs.stream().name("Fire")
        fires.forEach { fire ->
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
        fishSpots = Npcs.stream().filtered {
            rightSide(it)
        }.action("Harpoon").name("Fishing spot").list()
            .map { Pair(it, LocalPathFinder.findPath(it.tile().getWalkableNeighbor())) }
    }

    fun getFishSpot(spots: List<Pair<Npc, LocalPath>>): Npc? {
        val paths = spots.filter { !containsDangerousTile(it.second) }
        val doublePath = paths.filter { it.first.id() == DOUBLE_FISH_ID }.firstOrNull()
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
        Npcs.stream().filtered { it.tile().distanceTo(side.mastLocation) <= 5 }.name("Ammunition crate").firstOrNull()

    fun getBucketCrate(): GameObject? =
        Objects.stream(50).type(GameObject.Type.INTERACTIVE).filtered {
            it.tile().distanceTo(side.mastLocation) <= 5 || it.tile().distanceTo(side.bossPoolLocation) <= 5
        }.name("Buckets").nearest().firstOrNull()

    fun getWaterpump(): GameObject? =
        Objects.stream(50).type(GameObject.Type.INTERACTIVE).filtered {
            it.tile().distanceTo(side.mastLocation) <= 5 || it.tile().distanceTo(side.bossPoolLocation) <= 5
        }.name("Water pump").nearest().firstOrNull()

    fun getTetherPole(): GameObject? {
        val dest = Movement.destination()
        val destination = if (dest != Tile.Nil) dest else me.tile()
        val validTiles = listOf(side.totemLocation, side.mastLocation)
        return Objects.stream(50).type(GameObject.Type.INTERACTIVE).filtered {
            validTiles.contains(it.tile())
        }.action("Repair", "Tether").nearest(destination).firstOrNull()
    }

    fun getLadder(): GameObject? = Objects.stream().name("Rope ladder").action("Climb").firstOrNull()
}

fun main() {
    Tempoross().startScript()
}
