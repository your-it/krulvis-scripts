package org.powbot.krulvis.test

import org.powbot.krulvis.api.extensions.Skill
import org.powbot.krulvis.api.script.ATScript
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

    override fun rootComponent(): TreeComponent {
        return object : Leaf<TemporossDebug>(this, "TestLeaf") {
            override fun loop() {
                if (tempoross.side == Tempoross.Side.UNKNOWN) {
                    if (npcs.toStream().name("Ammunition crate").findFirst().isPresent) {
                        val mast = objects.toStream().name("Mast").nearest().first()
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
    }

    fun <E : InteractiveEntity> checkPaths(vararg entities: Optional<E>) {
        entities.forEach {
            if (it.isPresent) {
                tempoross.hasDangerousPath(it.get().tile())
            }
        }
    }

    override val painter: ATPainter<*>
        get() = TemporossDebugPainter(this)

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
        val clicked = ctx.game.crosshair() == Game.Crosshair.ACTION
        val lastClick = System.currentTimeMillis() - ctx.input.pressWhen
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

            drawTileOnScreen(g, script.cookSpot, Color.CYAN)
            drawTileOnScreen(g, script.tempoross.anchorLocation, Color.CYAN)
            drawTileOnScreen(g, script.tempoross.bossPoolLocation, Color.CYAN)
            drawTileOnScreen(g, script.tempoross.totemLocation, Color.CYAN)
            drawTileOnScreen(g, script.tempoross.bossWalkLocation, Color.CYAN)
            drawTileOnScreen(g, script.tempoross.mastLocation, Color.CYAN)

            val blockedTiles = script.tempoross.blockedTiles.toList()
            val paths = script.tempoross.triedPaths.toList()

            blockedTiles.forEach {
                val t = it
                if (t != Tile.NIL) {
                    drawTileOnScreen(g, it, Color.RED)
                }
            }
            if (paths.isNotEmpty()) {
                paths.map { it.actions.map { a -> a.destination } }.forEach { tiles ->
                    val containsBadTile = tiles.any { blockedTiles.contains(it) }
                    val color = if (containsBadTile) Color.ORANGE else Color.GREEN
                    tiles.forEach { tile ->
                        drawTileOnScreen(
                            g,
                            tile,
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
        var y = startY
        println("Saving image with y: $y")
        drawSplitText(g, "Side: ", script.tempoross.side.toString(), x, y)
        y += yy
        drawSplitText(g, "Animation: ", "${me.animation()}", x, y)
        y += yy
        val clicked = ctx.game.crosshair() == Game.Crosshair.ACTION
        val lastClick = System.currentTimeMillis() - ctx.input.pressWhen
        drawSplitText(g, "Click: $clicked", "Last: ${Timer.formatTime(lastClick)}", x, y)
    }

}