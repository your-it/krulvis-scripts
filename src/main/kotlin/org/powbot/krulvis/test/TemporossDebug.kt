package org.powbot.krulvis.test

import org.powbot.api.Color.BLACK
import org.powbot.api.Color.CYAN
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.ORANGE
import org.powbot.api.Color.RED
import org.powbot.api.InteractableEntity
import org.powbot.api.Nameable
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.Objects
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.api.script.tree.Leaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.mobile.drawing.Graphics
import java.util.*

@ScriptManifest(name = "Tempoross - Debug", description = "Some testing", version = "1.0")
class TemporossDebug : ATScript() {

    lateinit var tempoross: Tempoross

    var bucket = Optional.empty<GameObject>()
    var bossPool = Optional.empty<Npc>()
    var ammo = Optional.empty<Npc>()
    var tether = Optional.empty<GameObject>()
    var cookSpot = Tile.Nil


    fun <E : InteractableEntity> checkPaths(vararg entities: Optional<E>) {
        entities.forEach {
            if (it.isPresent) {
                tempoross.hasDangerousPath(it.get().tile())
            }
        }
    }

    override val painter: ATPainter<*> = TemporossDebugPainter(this)

    override val rootComponent: TreeComponent<*> = object : Leaf<TemporossDebug>(this, "TestLeaf") {
        override fun execute() {
            if (tempoross.side == Tempoross.Side.UNKNOWN) {
                if (Npcs.stream().name("Ammunition crate").findFirst().isPresent) {
                    val mast = Objects.stream().name("Mast").nearest().first()
                    println("Mast found: $mast, orientation: ${mast.orientation()}")
                    tempoross.side = if (mast.orientation() == 4) Tempoross.Side.SOUTH else Tempoross.Side.NORTH
                    tempoross.mastLocation = mast.tile()
                }
            } else if (tempoross.getEnergy() == -1) {
                println("Not in game...")
                tempoross.side = Tempoross.Side.UNKNOWN
            } else {
                tempoross.blockedTiles.clear()
                tempoross.triedPaths.clear()
                tempoross.detectDangerousTiles()


                val dest = Movement.destination()
                val destination = if (dest != Tile.Nil) dest else me.tile()
                val validTiles = listOf(tempoross.totemLocation, tempoross.mastLocation)
                val tetherpoles = Objects.stream().filter {
                    validTiles.contains(it.tile())
                }.forEach { println("Found ${it.name()}: ${it.actions().joinToString()}") }
                println(tetherpoles)
                cookSpot =
                    if (tempoross.side == Tempoross.Side.NORTH) tempoross.northCookSpot else tempoross.cookLocation
                // In game
                bucket = tempoross.getBucketCrate()
                ammo = tempoross.getAmmoCrate()
                bossPool = tempoross.getBossPool()
                tether = tempoross.getTetherPole()

                tempoross.collectFishSpots()
                tempoross.bestFishSpot = tempoross.getFishSpot(tempoross.fishSpots)
                checkPaths(bucket, tether)
                checkPaths(ammo, tempoross.bestFishSpot)
                tempoross.hasDangerousPath(tempoross.bossWalkLocation)

            }
            sleep(1000)
        }
    }

    init {
        skillTracker.addSkill(Skill.FISHING)
        tempoross = Tempoross()
    }

}

class TemporossDebugPainter(script: TemporossDebug) : ATPainter<TemporossDebug>(script, 10) {
    override fun paint(g: Graphics) {
        var y = this.y
        drawSplitText(g, "Side: ", script.tempoross.side.toString(), x, y)
        y += yy
        drawSplitText(g, "Animation: ", "${me.animation()}", x, y)
        y += yy
        drawSplitText(g, "Destination: ", "${Movement.destination()}", x, y)
        y += yy
//        val clicked = Ga.crosshair() == Game.Crosshair.ACTION
//        val lastClick = System.currentTimeMillis() - script.ctx.input.pressWhen
//        drawSplitText(g, "Click: $clicked", "Last: ${Timer.formatTime(lastClick)}", x, y)
//        y += yy

        if (script.tether.isPresent) {
            val tether = script.tether.get()
            drawSplitText(g, "Tether visible: ", tether.inViewport().toString(), x, y)
            y += yy
        }
        if (script.tempoross.side != Tempoross.Side.UNKNOWN) {
            drawEntity(g, script.bucket)
            drawEntity(g, script.bossPool)
            drawEntity(g, script.ammo)
            drawEntity(g, script.tether)
            drawEntity(g, script.tempoross.bestFishSpot)
            if (script.tether.isPresent) {
                val mm = script.tether.get().tile().mapPoint()
                g.drawString("TP", mm.x, mm.y)
            }

            script.cookSpot.drawOnScreen(g, null, CYAN)
            script.tempoross.anchorLocation.drawOnScreen(g, null, CYAN)
            script.tempoross.bossPoolLocation.drawOnScreen(g, null, CYAN)
            script.tempoross.totemLocation.drawOnScreen(g, null, CYAN)
            script.tempoross.bossWalkLocation.drawOnScreen(g, null, CYAN)
            script.tempoross.mastLocation.drawOnScreen(g, null, CYAN)

            val blockedTiles = script.tempoross.blockedTiles.toList()
            val paths = script.tempoross.triedPaths.toList()

            blockedTiles.forEach {
                val t = it
                if (t != Tile.Nil) {
                    it.drawOnScreen(g, null, RED)
                }
            }
            if (paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val containsBadTile = tiles.any { blockedTiles.contains(it) }
                    val color = if (containsBadTile) ORANGE else GREEN
                    tiles.forEach { tile ->
                        tile.drawOnScreen(
                            g,
                            null,
                            if (blockedTiles.contains(tile)) BLACK else color
                        )
                    }
                }
            }
        }
    }

    fun <E : InteractableEntity> drawEntity(g: Graphics, entity: Optional<E>) {
        if (entity.isPresent) {
            val e = entity.get()
            if (e.inViewport()) {
                val matrix = e.tile().matrix().bounds()
                g.drawPolygon(matrix)
                g.drawString((e as Nameable).name(), matrix.getBounds().centerX, matrix.getBounds().centerY)
            }
        }
    }

}