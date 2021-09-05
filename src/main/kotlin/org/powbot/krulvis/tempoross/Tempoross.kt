package org.powbot.krulvis.tempoross

import com.google.common.eventbus.Subscribe
import org.powbot.api.InteractableEntity
import org.powbot.api.Tile
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walk
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.OptionType
import org.powbot.api.script.ScriptConfiguration
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.PARENT_WIDGET
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Data.WAVE_TIMER
import org.powbot.krulvis.tempoross.tree.branch.ShouldEnterBoat
import java.util.*

@ScriptManifest(
    name = "krul Tempoross",
    description = "Does tempoross minigame",
    version = "1.1.2",
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
        )
    ]
)
class Tempoross : ATScript() {
    override val rootComponent: TreeComponent<*> = ShouldEnterBoat(this)

    init {
        skillTracker.addSkill(Skill.FISHING)
        debugComponents = false
    }

    override val painter: ATPainter<*> = TemporossPainter(this, if (debugComponents) 10 else 6)

    val waveTimer = Timer(0)
    var side: Side = Side.UNKNOWN
    var forcedShooting = false
    val blockedTiles = mutableListOf<Tile>()
    val triedPaths = mutableListOf<LocalPath>()
    var profile = TemporossProfile()
    var rewardGained = 0
    var pointsObtained = 0
    var rounds = 0
    var bestFishSpot: Npc? = null
    var fishSpots: List<Pair<Npc, LocalPath>> = emptyList()

    val cookFish by lazy { getOption<Boolean>("Cook fish") ?: true }

    fun hasDangerousPath(end: Tile): Boolean {
        val path = LocalPathFinder.findPath(end)
        return containsDangerousTile(path)
    }

    fun containsDangerousTile(path: LocalPath): Boolean {
        triedPaths.add(path)
        return path.actions.any { blockedTiles.contains(it.destination) }
    }

    fun interactWhileDousing(
        e: InteractableEntity?,
        action: String,
        destinationWhenNil: Tile,
        allowCrossing: Boolean
    ): Boolean {
        if (e == null) {
            debug("Can't find: $action")
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
                return interact(e as Interactive, action)
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
        val blockedTile = path.actions.firstOrNull { blockedTiles.contains(it.destination) }
        val fire =
            if (blockedTile != null) Npcs.stream().name("Fire").within(blockedTile.destination, 2.5).nearest()
                .firstOrNull() else null
        val hasBucket = Inventory.containsOneOf(BUCKET_OF_WATER)
        log.info("Blockedtile: $blockedTile fire: $fire, Bucket: $hasBucket")
        if (fire != null && hasBucket) {
            log.info("Found fire on the way to: ${path.finalDestination()}")
            if (!fire.inViewport()) {
                if (fire.distance() > 8) {
                    val blockedIndex = path.actions.indexOf(blockedTile)
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

    fun canKill(): Boolean = getEnergy() in 0..2 || getBossPool() != null

    fun isTethering(): Boolean =
        Objects.stream().action("Untether").isNotEmpty() || me.animation() == Data.TETHER_ANIM

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
        println(txt)
        if (txt.contains("Points: ") && txt.contains("Personal best", true)) {
            val points = txt.substring(20, txt.indexOf("</col>")).replace(",", "").toInt()
            println("Finished round, gained: $points points")
            pointsObtained += points
            rounds++
        } else if (txt.contains("A colossal wave closes in...")) {
            println("Should tether wave coming in!")
            waveTimer.reset(WAVE_TIMER)
            val fishId = if (cookFish) COOKED else RAW
            val fish = Inventory.stream().id(fishId).count()
            if (profile.shootAfterTethering && (fish >= profile.minFishToForceShoot || fish >= getHealth())) {
                forcedShooting = true
            }
        } else if (txt.contains("Reward permits: ") && txt.contains("Total permits:")) {
            val reward = txt.substring(28, txt.indexOf("</col>")).toInt()
            println("Gained $reward points")
            rewardGained += reward
        }
    }

    enum class Side {
        UNKNOWN,
        NORTH,
        SOUTH
    }


    var mastLocation = Tile(9618, 2817, 0)
    val oddFishingSpot: Tile get() = Tile(mastLocation.x() - 21, mastLocation.y() - 15, 0)

    val cookLocation: Tile
        get() = if (side == Side.NORTH)
            Tile(mastLocation.x() + 4, mastLocation.y() + 25, 0)
        else Tile(mastLocation.x() - 22, mastLocation.y() - 21, 0)

    val northCookSpot: Tile get() = Tile(cookLocation.x() + 1, cookLocation.y() - 3, 0)

    val bossPoolLocation: Tile
        get() = if (side == Side.NORTH)
            Tile(mastLocation.x() + 11, mastLocation.y() + 5, 0)
        else Tile(mastLocation.x() - 11, mastLocation.y() - 5, 0)

    val bossWalkLocation: Tile
        get() = if (side == Side.NORTH)
            Tile(bossPoolLocation.x(), bossPoolLocation.y() + 2, 0)
        else
            Tile(bossPoolLocation.x(), bossPoolLocation.y() - 2, 0)

    val totemLocation: Tile
        get() = if (side == Side.NORTH)
            Tile(mastLocation.x() + 8, mastLocation.y() + 18, 0)
        else Tile(mastLocation.x() - 15, mastLocation.y() - 16, 0)

    val anchorLocation: Tile
        get() = if (side == Side.NORTH)
            Tile(mastLocation.x() + 8, mastLocation.y() + 9, 0)
        else
            Tile(mastLocation.x() - 8, mastLocation.y() - 9, 0)

    fun rightSide(spot: Npc): Boolean {
        return if (side == Side.NORTH) {
            spot.tile().y() > mastLocation.y()
        } else {
            spot.tile().y() < mastLocation.y()
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
        blockedTiles.add(tile)
        blockedTiles.add(Tile(tile.x(), tile.y() - 1, 0))
        blockedTiles.add(Tile(tile.x() - 1, tile.y(), 0))
        blockedTiles.add(Tile(tile.x() - 1, tile.y() - 1, 0))
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
        Npcs.stream().at(bossPoolLocation).action("Harpoon").name("Spirit pool").firstOrNull()

    fun getAmmoCrate(): Npc? =
        Npcs.stream().filtered { it.tile().distanceTo(mastLocation) <= 5 }.name("Ammunition crate").firstOrNull()

    fun getBucketCrate(): GameObject? =
        Objects.stream().filtered {
            it.tile().distanceTo(mastLocation) <= 5 || it.tile().distanceTo(bossPoolLocation) <= 5
        }.name("Buckets").nearest().firstOrNull()

    fun getWaterpump(): GameObject? =
        Objects.stream().filtered {
            it.tile().distanceTo(mastLocation) <= 5 || it.tile().distanceTo(bossPoolLocation) <= 5
        }.name("Water pump").nearest().firstOrNull()

    fun getTetherPole(): GameObject? {
        val dest = Movement.destination()
        val destination = if (dest != Tile.Nil) dest else me.tile()
        val validTiles = listOf(totemLocation, mastLocation)
        return Objects.stream().filtered {
            validTiles.contains(it.tile())
        }.action("Repair", "Tether").nearest(destination).firstOrNull()
    }

    fun getLadder(): GameObject? = Objects.stream().name("Rope ladder").action("Climb").firstOrNull()
}

fun main() {
    Tempoross().startScript()
}
