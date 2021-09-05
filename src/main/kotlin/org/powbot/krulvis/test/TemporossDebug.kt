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
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.api.script.tree.Leaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.tempoross.Data
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.input.Touchscreen
import java.util.*

@ScriptManifest(name = "Tempoross - Debug", description = "Some testing", version = "1.0")
class TemporossDebug : ATScript() {

    lateinit var tempoross: Tempoross

    var bucket: GameObject? = null
    var bossPool: Npc? = null
    var ammo: Npc? = null
    var tether: GameObject? = null
    var cookSpot = Tile.Nil


    fun checkPaths(vararg entities: InteractableEntity?) {
        entities.forEach {
            if (it != null) {
                tempoross.hasDangerousPath(it.tile())
            }
        }
    }

    override val painter: ATPainter<*> = TemporossDebugPainter(this)

    fun debugComp(c: Component?) {
        if (c == null) {
            log.info("Comp == null")
            return
        }
        val parent = c.parent()
        log.info("Widget=[${c.widgetId()}], Parent=[${parent} id=${c.parentId()}], $c, Hidden=${c.hidden()}, Valid=${c.valid()}, Text=${c.text()}, Boundsindex=${c.boundsIndex()}")
        if (parent != null) debugComp(parent)
    }

    override val rootComponent: TreeComponent<*> = object : Leaf<TemporossDebug>(this, "TestLeaf") {
        override fun execute() {
            val c =
                Components.stream(Data.PARENT_WIDGET).filtered { it.text().contains("Energy") }.firstOrNull()
            debugComp(c)
            if (tempoross.side == Side.UNKNOWN) {
                if (Npcs.stream().name("Ammunition crate").findFirst().isPresent) {
                    val mast = Objects.stream().name("Mast").nearest().first()
                    println("Mast found: $mast, orientation: ${mast.orientation()}")
                    tempoross.side = if (mast.orientation() == 4) Side.SOUTH else Side.NORTH
                    tempoross.side.mastLocation = mast.tile()
                }
            } else if (tempoross.getEnergy() == -1) {
                println("Not in game...")
                tempoross.side = Side.UNKNOWN
            } else {
                //In game
                val pole = Objects.stream(3).action("Untether").firstOrNull()
                val mast = Objects.stream().name("Mast").nearest().firstOrNull()
                val atMast = Objects.stream().at(tempoross.side.mastLocation).list()
                log.info("Untether pole: $pole")
                log.info("Mast: $mast, distance=${mast?.distance()}, actions=${mast?.actions()?.joinToString()}")
                atMast.forEach {
                    log.info("Found obj at mast: ${it.name()}, actions=${it.actions().joinToString()}")
                }

                tempoross.blockedTiles.clear()
                tempoross.triedPaths.clear()
                tempoross.detectDangerousTiles()


                val dest = Movement.destination()
                val destination = if (dest != Tile.Nil) dest else me.tile()
                val validTiles = listOf(tempoross.side.totemLocation, tempoross.side.mastLocation)
                val tetherpoles = Objects.stream().filtered {
                    validTiles.contains(it.tile())
                }.forEach { println("Found ${it.name()}: ${it.actions().joinToString()}") }
                println(tetherpoles)
                cookSpot = tempoross.side.cookLocation
                // In game
                bucket = tempoross.getBucketCrate()
                ammo = tempoross.getAmmoCrate()
                bossPool = tempoross.getBossPool()
                tether = tempoross.getTetherPole()

                tempoross.collectFishSpots()
                tempoross.bestFishSpot = tempoross.getFishSpot(tempoross.fishSpots)
                checkPaths(bucket, tether)
                checkPaths(ammo, tempoross.bestFishSpot)
                tempoross.hasDangerousPath(tempoross.side.bossWalkLocation)

            }
            sleep(1000)
        }
    }

    init {
        skillTracker.addSkill(Skill.FISHING)
        tempoross = Tempoross()
    }

}

class TemporossDebugPainter(script: TemporossDebug) : ATPainter<TemporossDebug>(script, 10, 350) {
    init {
        x = 400
    }

    override fun paint(g: Graphics, startY: Int): Int {
        var y = startY
        y = drawSplitText(g, "Side: ", script.tempoross.side.toString(), x, y)
        y = drawSplitText(g, "Animation: ", "${me.animation()}", x, y)
        y = drawSplitText(g, "Destination: ", "${Movement.destination()}", x, y)
        y = drawSplitText(g, "Tethering: ", "${script.tempoross.isTethering()}", x, y)
        y = drawSplitText(
            g,
            "Energy: ${script.tempoross.getEnergy()}",
            "Health: ${script.tempoross.getHealth()}: ",
            x,
            y
        )
        val tether = script.tether
//        val clicked = Ga.crosshair() == Game.Crosshair.ACTION
//        val lastClick = System.currentTimeMillis() - script.ctx.input.pressWhen
//        drawSplitText(g, "Click: $clicked", "Last: ${Timer.formatTime(lastClick)}", x, y)
//        y += yy

        if (tether != null) {
            y = drawSplitText(g, "Tether visible: ", tether.inViewport().toString(), x, y)
        }
        if (script.tempoross.side != Side.UNKNOWN) {
            drawEntity(g, script.bucket)
            drawEntity(g, script.bossPool)
            drawEntity(g, script.ammo)
            drawEntity(g, script.tether)
            drawEntity(g, script.tempoross.bestFishSpot)

            if (tether != null) {
                val mm = tether.tile().mapPoint()
                g.drawString("TP", mm.x, mm.y)
            }

//            script.cookSpot.drawOnScreen(g, null, CYAN)
//            script.tempoross.side.anchorLocation.drawOnScreen(g, null, CYAN)
//            script.tempoross.side.bossPoolLocation.drawOnScreen(g, null, CYAN)
//            script.tempoross.side.totemLocation.drawOnScreen(g, null, CYAN)
//            script.tempoross.side.bossWalkLocation.drawOnScreen(g, null, CYAN)
//            script.tempoross.side.mastLocation.drawOnScreen(g, null, CYAN)

//            val blockedTiles = script.tempoross.blockedTiles.toList()
//            val paths = script.tempoross.triedPaths.toList()
//
//            blockedTiles.forEach {
//                val t = it
//                if (t != Tile.Nil) {
//                    it.drawOnScreen(g, null, RED)
//                }
//            }
//            if (paths.isNotEmpty()) {
//                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
//                    val containsBadTile = tiles.any { blockedTiles.contains(it) }
//                    val color = if (containsBadTile) ORANGE else GREEN
//                    tiles.forEach { tile ->
//                        tile.drawOnScreen(
//                            g,
//                            null,
//                            if (blockedTiles.contains(tile)) BLACK else color
//                        )
//                    }
//                }
//            }
        }
        return y
    }

    fun drawEntity(g: Graphics, e: InteractableEntity?) {
        if (e != null) {
            if (e.inViewport()) {
                val matrix = Touchscreen.scaleFromGame(e.tile().matrix().bounds())
                g.drawPolygon(matrix)
                g.drawString((e as Nameable).name() ?: "null", matrix.getBounds().centerX, matrix.getBounds().centerY)
            }
        }
    }

}

fun main() {
    TemporossDebug().startScript()
}