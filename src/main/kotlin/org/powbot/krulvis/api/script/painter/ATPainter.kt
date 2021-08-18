package org.powbot.krulvis.api.script.painter

import org.powbot.api.Color
import org.powbot.api.Color.BLACK
import org.powbot.api.Color.BLACK_A
import org.powbot.api.Color.GREEN
import org.powbot.api.Color.ORANGE
import org.powbot.api.Color.WHITE
import org.powbot.api.Rectangle
import org.powbot.api.Tile
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Timer
import org.powbot.mobile.drawing.Graphics
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.roundToInt


abstract class ATPainter<S : ATScript>(val script: S, val lines: Int = 0, val width: Int = 350) {

    val useLayout = lines > 0
    private var username: String? = null
    var x = 15
    var y = 90
    val custom = DynamicColor(0.40f, 0.75f, 0.01f)

    abstract fun paint(g: Graphics, startY: Int)

    fun onRepaint(g: Graphics) {
        try {
            if (useLayout) {
                val y = drawLayout(g)
                g.setColor(ORANGE)
                paint(g, y)
            } else
                paint(g, y)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLayoutHeight(): Int = 51 + lines * 20

    fun drawLayout(g: Graphics): Int {

        var x = this.x - 5
        val h = getLayoutHeight()
        var y = this.y
        val mid = (x + width / 2.0).toInt()

        g.setColor(bgColor())
        g.fillRect(x, y, width, h)
        val dynColor = custom[true]
        g.setColor(dynColor)
        g.drawRect(x, y, width, h)

        drawTitle(g, mid, y)

        y += 20
        x += 5
        y = drawSplitText(
            g,
            "Runtime: ",
            Timer.formatTime(script.timer.getElapsedTime()),
            x,
            y,
            width
        )
        return y
    }

    open fun bgColor(): Int = BLACK_A

    open fun drawTitle(g: Graphics, x: Int, y: Int) {
        drawTitle(g, script.manifest.name, x, y)
    }

    private var chatBoxUsernameBox: org.powbot.api.Point? = null

    fun drawTile(
        g: Graphics,
        t: Tile,
        text: String? = null,
        lineColor: Int? = GREEN,
        fillColor: Int? = null,
        mapColor: Int = GREEN
    ) {
        t.drawOnMap(g, mapColor)
        t.drawOnScreen(
            g,
            text,
            lineColor,
            fillColor
        )
    }

    fun drawSplitText(
        g: Graphics,
        desc: String,
        value: String,
        x: Int,
        y: Int
    ): Int {
        return drawSplitText(g, desc, value, x, y, width)
    }

    companion object {

        val yy = 18

        fun drawSplitText(
            g: Graphics,
            desc: String,
            value: String,
            x: Int,
            y: Int,
            width: Int,
            color: Int = WHITE
        ): Int {
            g.setTextSize(11f)
            val valueWidth = g.getTextWidth(value)
            drawShadowedText(g, desc, x, y, false, color)
            drawShadowedText(g, value, width - valueWidth.toInt() + x - 6, y, false, color)
            g.setTextSize(14f)
            return y + yy
        }

        fun drawTitle(g: Graphics, text: String, x: Int, y: Int) {
            val textSize = g.getTextSize()
            g.setTextSize(20f)
            val width = g.getTextWidth(text)
            val x = (x - width / 2.0).toInt()
//            g.setColor(WHITE)
            g.drawString(text, x - 1, y - 1)
            g.drawString(text, x + 1, y + 1)
            g.drawString(text, x + 1, y - 1)
            g.drawString(text, x - 1, y + 1)
            g.setColor(BLACK)
            g.drawString(text, x, y)
            g.setTextSize(textSize)
        }

        fun Tile.drawOnMap(g: Graphics, color: Int = GREEN) {
            val oldc = g.getColor()
            val center = Rectangle(mapPoint().x, mapPoint().y, 3, 3)
            if (center.x > 0 && center.y > 0) {
                g.setColor(color)
                g.drawRect(center)
            }
            g.setColor(oldc)
        }

        fun Tile.drawCircleOnMap(g: Graphics, radius: Int) {
            val centerPoint = mapPoint()
            val mmRadius = centerPoint.distance(Tile(x() + radius, y() + radius, 0).mapPoint())
            g.drawOval(centerPoint.x, centerPoint.y, mmRadius, mmRadius)
        }

        fun Tile.drawOnScreen(
            g: Graphics,
            text: String? = null,
            outlineColor: Int? = GREEN,
            fillColor: Int? = null
        ) {
            val c = g.getColor()
            val p = matrix().bounds()
            if (outlineColor != null) {
                g.setColor(outlineColor)
                g.drawPolygon(p)
            }
            if (fillColor != null) {
                g.setColor(fillColor)
                g.fillRect(p.getBounds())
            }
            if (text != null)
                g.drawString(text, p.getBounds().centerX, p.getBounds().centerY)

            g.setColor(c)
        }

//        fun Tile.drawCollisions(g: Graphics2D, flags: Array<IntArray>) {
//            val bounds = matrix()?.bounds() ?: return
//            val flag = collisionFlag(flags)
//            if (rockfallBlock(flags)) {
//                drawOnScreen(g, flag.toString(), Color.LIGHT_GRAY)
//            } else if (blocked(flags)) {
//                drawOnScreen(g, flag.toString(), Color.RED)
//            } else {
//                g.drawString(flag.toString(), bounds.getBounds().centerX.toInt(), bounds.bounds.centerY.toInt())
//                val south = Line2D.Double(
//                    Point(bounds.xpoints[0], bounds.ypoints[0]),
//                    Point(bounds.xpoints[1], bounds.ypoints[1])
//                )
//                val east = Line2D.Double(
//                    Point(bounds.xpoints[1], bounds.ypoints[1]),
//                    Point(bounds.xpoints[2], bounds.ypoints[2])
//                )
//                val north = Line2D.Double(
//                    Point(bounds.xpoints[2], bounds.ypoints[2]),
//                    Point(bounds.xpoints[3], bounds.ypoints[3])
//                )
//                val west = Line2D.Double(
//                    Point(bounds.xpoints[3], bounds.ypoints[3]),
//                    Point(bounds.xpoints[0], bounds.ypoints[0])
//                )
//                g.color = if (flag and Flag.W_S == 0) Color.GREEN else Color.RED
//                g.draw(south)
//
//                g.color = if (flag and Flag.W_E == 0) Color.GREEN else Color.RED
//                g.draw(east)
//
//                g.color = if (flag and Flag.W_N == 0) Color.GREEN else Color.RED
//                g.draw(north)
//
//                g.color = if (flag and Flag.W_W == 0) Color.GREEN else Color.RED
//                g.draw(west)
//            }
//        }

        fun drawShadowedText(
            g: Graphics,
            text: String,
            x: Int,
            y: Int,
            doubleShadow: Boolean = false,
            color: Int = ORANGE,
        ) {
            g.setColor(BLACK)
            if (doubleShadow) {
                g.drawString(text, x + 2, y - 2)
            }
            g.drawString(text, x + 1, y - 1)
            g.setColor(color)
            g.drawString(text, x, y)
        }


        fun format(i: Int): String = DECIMAL_FORMAT.format(i.toLong())

        fun formatAmount(amount: Int): String {
            var a = amount
            val needsMin = a < 0
            a = abs(a)
            if (a / 10000000 > 0) {
                return (a.toDouble() / 1000000.0).roundToInt().toString() + "m"
            }
            return if (a / 1000 > 0) {
                (a.toDouble() / 1000.0).roundToInt().toString() + "k"
            } else (if (needsMin) "-" else "") + a + ""
        }

        val DECIMAL_FORMAT = DecimalFormat("###,###,###.###")


    }


}