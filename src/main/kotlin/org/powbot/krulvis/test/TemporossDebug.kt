package org.powbot.krulvis.test

import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.tempoross.Tempoross
import org.powerbot.script.*
import org.powerbot.script.rt4.Game
import org.powerbot.script.rt4.GameObject
import org.powerbot.script.rt4.Npc
import java.awt.Color
import java.awt.Graphics2D
import java.util.*

@Script.Manifest(name = "Tempoross - Debug", description = "Some testing", version = "1.0")
class TemporossDebug : ATScript() {

    lateinit var tempoross: Tempoross

    var bucket = Optional.empty<GameObject>()
    var bossPool = Optional.empty<Npc>()
    var ammo = Optional.empty<Npc>()
    var tether = Optional.empty<GameObject>()
    var cookSpot = Tile.NIL


    fun <E : InteractiveEntity> checkPaths(vararg entities: Optional<E>) {
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
                if (ctx.npcs.toStream().name("Ammunition crate").findFirst().isPresent) {
                    val mast = ctx.objects.toStream().name("Mast").nearest().first()
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


                val dest = ctx.movement.destination()
                val destination = if (dest != null && dest != Tile.NIL) dest else me.tile()
                val validTiles = listOf(tempoross.totemLocation, tempoross.mastLocation)
                val tetherpoles = ctx.objects.toStream().filter {
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

    override fun startGUI() {
        skillTracker.addSkill(Skill.FISHING)
        debugComponents = true
        started = true
        tempoross = Tempoross()
    }

}

class TemporossDebugPainter(script: TemporossDebug) : ATPainter<TemporossDebug>(script, 10) {
    override fun paint(g: Graphics2D) {
        var y = this.y
        drawSplitText(g, "Side: ", script.tempoross.side.toString(), x, y)
        y += yy
        drawSplitText(g, "Animation: ", "${me.animation()}", x, y)
        y += yy
        drawSplitText(g, "Destination: ", "${script.ctx.movement.destination()}", x, y)
        y += yy
        val clicked = script.ctx.game.crosshair() == Game.Crosshair.ACTION
        val lastClick = System.currentTimeMillis() - script.ctx.input.pressWhen
        drawSplitText(g, "Click: $clicked", "Last: ${Timer.formatTime(lastClick)}", x, y)
        y += yy

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

            script.cookSpot.drawOnScreen(g, null, Color.CYAN)
            script.tempoross.anchorLocation.drawOnScreen(g, null, Color.CYAN)
            script.tempoross.bossPoolLocation.drawOnScreen(g, null, Color.CYAN)
            script.tempoross.totemLocation.drawOnScreen(g, null, Color.CYAN)
            script.tempoross.bossWalkLocation.drawOnScreen(g, null, Color.CYAN)
            script.tempoross.mastLocation.drawOnScreen(g, null, Color.CYAN)

            val blockedTiles = script.tempoross.blockedTiles.toList()
            val paths = script.tempoross.triedPaths.toList()

            blockedTiles.forEach {
                val t = it
                if (t != Tile.NIL) {
                    it.drawOnScreen(g, null, Color.RED)
                }
            }
            if (paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val containsBadTile = tiles.any { blockedTiles.contains(it) }
                    val color = if (containsBadTile) Color.ORANGE else Color.GREEN
                    tiles.forEach { tile ->
                        tile.drawOnScreen(
                            g,
                            null,
                            if (blockedTiles.contains(tile)) Color.BLACK else color
                        )
                    }
                }
            }
        }
    }

    fun <E : InteractiveEntity> drawEntity(g: Graphics2D, entity: Optional<E>) {
        if (entity.isPresent) {
            val e = entity.get()
            if (e.inViewport()) {
                val matrix = e.tile().matrix(script.ctx).bounds()
                g.draw(matrix)
                g.drawString((e as Nameable).name(), matrix.bounds.centerX.toInt(), matrix.bounds.centerY.toInt())
            }
        }
    }

    override fun drawProgressImage(g: Graphics2D, startY: Int) {

    }

}