package org.powbot.krulvis.tempoross

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.walking.local.LocalPath
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.PARENT_WIDGET
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Data.WAVE_TIMER
import org.powbot.krulvis.tempoross.tree.branch.ShouldEnterBoat
import org.powerbot.script.*
import org.powerbot.script.rt4.GameObject
import org.powerbot.script.rt4.Interactive
import org.powerbot.script.rt4.Npc
import java.util.*
import kotlin.streams.toList

@Script.Manifest(name = "Tempoross", description = "Does tempoross minigame", version = "1.0")
class Tempoross : ATScript(), MessageListener {


    override fun rootComponent(): TreeComponent {
        return ShouldEnterBoat(this)
    }

    override val painter: ATPainter<*> = TemporossPainter(this)
    override fun startGUI() {
        println("No GUI so we're starting")
        started = true
        debugComponents = true
        skillTracker.addSkill(Skill.FISHING)
    }

    val waveTimer = Timer(0)
    var side: Side = Side.UNKNOWN
    var forcedShooting = false
    val blockedTiles = mutableListOf<Tile>()
    val triedPaths = mutableListOf<LocalPath>()
    var profile = TemporossProfile()
    var rewardGained = 0
    var pointsObtained = 0
    var rounds = 0
    var bestFishSpot: Optional<Npc> = Optional.empty()
    var fishSpots: List<Pair<Npc, LocalPath>> = emptyList()

    fun hasDangerousPath(end: Tile): Boolean {
        val path = lpf.findPath(end)
        return containsDangerousTile(path)
    }

    fun containsDangerousTile(path: LocalPath): Boolean {
        triedPaths.add(path)
        return path.actions.any { blockedTiles.contains(it.destination) }
    }

    fun <E : InteractiveEntity> interactWhileDousing(
        optional: Optional<E>,
        action: String,
        destinationWhenNil: Tile,
        allowCrossing: Boolean
    ): Boolean {
        if (optional.isEmpty) {
            debug("Can't find: ${if (optional.isEmpty) action else (optional.get() as Nameable).name()}")
            if (destinationWhenNil != Tile.NIL) {
                val path = lpf.findPath(destinationWhenNil)
                if (path.isNotEmpty() && douseIfNecessary(path, allowCrossing)) {
                    walkPath(path)
                } else {
                    walk(destinationWhenNil)
                }
            }
        } else {
            val e = optional.get()
            var path = lpf.findPath(e.tile())
            if (path.isEmpty()) path = lpf.findPath(destinationWhenNil)
            if (douseIfNecessary(path, allowCrossing)) {
                return interact(e as Interactive, action)
            }
        }
        return false
    }

    fun walkWhileDousing(path: LocalPath, allowCrossing: Boolean): Boolean {
        if (douseIfNecessary(path, allowCrossing)) {
            return walkPath(path)
        }
        return true
    }

    fun douseIfNecessary(path: LocalPath, allowCrossing: Boolean = false): Boolean {
        val blockedTile = path.actions.firstOrNull { blockedTiles.contains(it.destination) }
        val fireOptional =
            if (blockedTile != null) objects.toStream().name("Fire").filter {
                it.tile().distanceTo(blockedTile.destination) <= 2
            }.nearest().findFirst() else Optional.empty()
        if (fireOptional.isPresent && inventory.contains(BUCKET_OF_WATER)) {
            val fire = fireOptional.get()
            debug("Found fire on the way to: ${path.finalDestination()}")
            if (!fire.inViewport()) {
                if (fire.distance() > 8) {
                    val blockedIndex = path.actions.indexOf(blockedTile)
                    val safeSpot = path.actions[blockedIndex - 2].destination
                    walking.step(safeSpot)
                }
                camera.turnTo(fire)
                waitFor(long()) { fire.inViewport() }
            } else if (fire.interact("Douse")) {
                return waitFor(long()) { objects.toStream().at(fire).name("Fire").isEmpty() }
            }
        } else if (!fireOptional.isPresent || allowCrossing) {
            debug("No fire on the way")
            return true
        }
        return false
    }

    fun walkPath(path: LocalPath): Boolean {
        val finalTile = path.actions.last().destination
        return if (finalTile.matrix(ctx).onMap() && finalTile.distance() > 5) {
            walking.step(finalTile)
        } else if (path.isNotEmpty()) {
            path.traverse()
        } else {
            println("Trying walkPath but got empty path...")
            false
        }
    }

    fun canKill(): Boolean = getEnergy() in 0..2 || getBossPool().isPresent

    fun isTethering(): Boolean =
        objects.toStream().action("Untether").isNotEmpty() || me.animation() == Data.TETHER_ANIM

    fun getEnergy(): Int {
        val text = widgets.widget(PARENT_WIDGET).firstOrNull { it.visible() && it.text().contains("Energy") }?.text()
            ?: return -1
        return text.substring(8, text.indexOf("%")).toInt()
    }

    fun getHealth(): Int {
        val text = widgets.widget(PARENT_WIDGET).firstOrNull { it.visible() && it.text().contains("Essence") }?.text()
            ?: return -1
        return text.substring(9, text.indexOf("%")).toInt()
    }

    override fun messaged(me: MessageEvent?) {
        val me = me ?: return
        if (me.type() != 0) {
            return
        }
        val txt = me.text()
        if (txt.contains("You gained ") && txt.contains("points")) {
            println(txt)
            val points = txt.substring(23, txt.indexOf("</col>")).toInt()
            println("Finished round, gained: $points points")
            pointsObtained += points
            rounds++
        } else if (txt.contains("A colossal wave closes in...")) {
            println("Should tether wave coming in!")
            waveTimer.reset(WAVE_TIMER)
            val fishId = if (profile.cook) COOKED else RAW
            val fish = inventory.toStream().id(fishId).count()
            if (profile.shootAfterTethering && (fish >= profile.minFishToForceShoot || fish >= getHealth())) {
                forcedShooting = true
            }
        } else if (txt.contains("You have earned ") && txt.contains("You now have")) {
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

    val northCookSpot: Tile get() = Tile(cookLocation.x() + 1, cookLocation.y() - 1, 0)

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
        npcs.toStream().name("Lightning cloud").filter { it.animation() > 0 }.forEach {
            addTile(it.tile())
        }

        val fires = objects.toStream().name("Fire")
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
        val allSpots = npcs.toStream().action("Harpoon").name("Fishing spot").filter {
            rightSide(it)
        }.toList()
        fishSpots = allSpots.map { Pair(it, lpf.findPath(it.tile().getWalkableNeighbor())) }
    }

    fun getFishSpot(spots: List<Pair<Npc, LocalPath>>): Optional<Npc> {
        val paths = spots.filter { !containsDangerousTile(it.second) }
        val doublePath = paths.firstOrNull { it.first.id() == DOUBLE_FISH_ID }
        if (doublePath != null) {
            return Optional.of(doublePath.first)
        }
        val minPath = paths.minByOrNull { it.second.actions.size }
        return if (minPath != null) Optional.of(minPath.first) else Optional.empty()
    }

    fun getBossPool() =
        npcs.toStream().at(bossPoolLocation).action("Harpoon").name("Spirit pool").findFirst()

    fun getAmmoCrate(): Optional<Npc> =
        npcs.toStream().name("Ammunition crate").filter { it.tile().distanceTo(mastLocation) <= 5 }.findFirst()

    fun getBucketCrate(): Optional<GameObject> =
        objects.toStream().name("Buckets").filter {
            it.tile().distanceTo(mastLocation) <= 5 || it.tile().distanceTo(bossPoolLocation) <= 5
        }.nearest().findFirst()

    fun getWaterpump(): Optional<GameObject> =
        objects.toStream().name("Water pump").filter {
            it.tile().distanceTo(mastLocation) <= 5 || it.tile().distanceTo(bossPoolLocation) <= 5
        }.nearest().findFirst()

    fun getTetherPole(): Optional<GameObject> {
        val destination = if (walking.destination() != null) walking.destination() else me.tile()
        val validTiles = listOf(totemLocation, mastLocation)
        return objects.toStream().action("Repair", "Tether").filter {
            validTiles.contains(it.tile())
        }.nearest(destination).nearest().findFirst()
    }

    fun getLadder(): Optional<GameObject> = objects.toStream().action("Climb").name("Rope ladder").findFirst()
}