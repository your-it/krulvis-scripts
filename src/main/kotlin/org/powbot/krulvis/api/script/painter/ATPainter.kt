package org.powbot.krulvis.api.script.painter

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.debugComponents
import org.powbot.krulvis.api.ATContext.mapPoint
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.walking.Flag
import org.powbot.krulvis.api.extensions.walking.PathFinder.Companion.rockfallBlock
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.resources.ATFont
import org.powbot.krulvis.api.utils.resources.ATFont.Companion.RUNESCAPE_FONT
import org.powbot.krulvis.api.utils.resources.ATImage.Companion.SKULL_GIF
import org.powerbot.script.ClientContext
import org.powerbot.script.Tile
import java.awt.*
import java.awt.geom.Ellipse2D
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.io.File
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.imageio.ImageIO
import kotlin.math.abs
import kotlin.math.roundToInt


abstract class ATPainter<S : ATScript>(val script: S, val lines: Int = 0, val width: Int = 200) {

    val useLayout = lines > 0
    var hideUsername = true
    private var username: String? = null
    val x = 15
    val lowestY = 335
    var y = 335
    var yy = 13
    val custom = DynamicColor(0.40f, 0.75f, 0.01f)

    abstract fun paint(g: Graphics2D)

    fun onRepaint(g: Graphics2D) {
        try {
            if (useLayout) {
                y = drawLayout(g)
            }
            //AntiBan.paintBreak(g);
            g.color = Color.ORANGE
            paint(g)
            if (debugComponents) {
                drawMouse(g)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getLayoutHeight(): Int = 51 + lines * 10

    fun drawLayout(g1: Graphics, lowestY: Int = this.lowestY): Int {
        val g = g1 as Graphics2D
        if (hideUsername) {
            hideChatboxName(g)
        }
        g.stroke = BasicStroke(1.0f)
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF)

        var x = 10
        val h = getLayoutHeight()
        var y = lowestY - h
        val mid = (x + width / 2.0).toInt()
        val skullWidth = 25
        val skullHeight = (skullWidth * 1.26).toInt()


        g.color = BLACK_A
        g.fillRoundRect(x, y, width, h, 5, 5)
        g.color = Color.WHITE
        g.drawRoundRect(x, y, width, h, 5, 5)

        drawTitle(g, mid - 3, y)

        y += 20
        x += 5
        drawSplitText(
            g,
            "Runtime: ",
            Timer.formatTime(script.timer.getElapsedTime()),
            x,
            y,
            width
        )
        y += 15
//        g.drawImage(skull, w - x, startY - skullHeight - 2, skullWidth, skullHeight, null)
        g.drawImage(
            SKULL_GIF.getAnimatedImage(),
            mid - skullWidth / 2 - 3,
            lowestY - h - 6,
            skullWidth,
            skullHeight,
            null
        )
        return y
    }

    fun saveProgressImage(): BufferedImage {
        val height = getLayoutHeight() + 70
        val width = width + 20
        val mid = (x + width / 2.0).toInt()
        val img = BufferedImage(width, height, 1)
        val g = img.graphics as Graphics2D
        val y = drawLayout(g, height - 5)
        drawProgressImage(g, y)
        val mobile = ctx.client().isMobile
        g.drawString("Client: ${if (mobile) "Mobile" else "Desktop"}", mid + 1, height - 8)

        val cal = Calendar.getInstance()

        val timeOnly = SimpleDateFormat("dd-MM-yyyy_HH-mm-ss")
        val timeStamp = timeOnly.format(cal.time)

        val file = File(
            proggyFolder().absolutePath + File.separator +
                    "${script.name}_$timeStamp.png"
        )
        ImageIO.write(img, "png", file)
        return img
    }

    open fun drawTitle(g: Graphics2D, x: Int, y: Int) {
        drawTitle(g, script.manifest?.name ?: "NoManifest", x, y)
    }

    private var chatBoxUsernameBox: Point? = null

    private fun hideChatboxName(g: Graphics, color: Color = Color.BLACK) {
        if (username == null && script.ctx.game.loggedIn()) {
            username = me.name()
        }
        if (username != null) {
            val username = username!!.replace("_", " ")
            if (chatBoxUsernameBox == null || ctx.game.resizable()) {
                val wc =
                    ctx.widgets.widget(162).find { it.text().replace(160.toChar(), ' ').contains(username) } ?: return
                chatBoxUsernameBox = wc.screenPoint()
            }
            g.color = color
            g.fillRect(chatBoxUsernameBox!!.x, chatBoxUsernameBox!!.y, username.length * 7, 15)
        }
    }

    private fun drawMouse(g: Graphics2D) {
        val oldCol = g.color
        g.color = Color.LIGHT_GRAY
        val ml = ctx.input.location
        val shape = Ellipse2D.Double(ml.getX() - 4, ml.getY() - 4, 8.0, 8.0)
        g.fill(shape)
        g.color = Color.DARK_GRAY
        g.draw(shape)
        g.color = oldCol
    }

    fun drawTile(
        g: Graphics2D,
        t: Tile,
        text: String? = null,
        lineColor: Color? = Color.GREEN,
        fillColor: Color? = null,
        mapColor: Color = Color.GREEN
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
        g: Graphics2D,
        desc: String,
        value: String,
        x: Int,
        y: Int
    ) {
        drawSplitText(g, desc, value, x, y, width)
    }

    private fun proggyFolder(): File {
        val f = File(script.powbotFolder() + File.separator + "proggies")
        if (!f.exists()) f.mkdirs()
        return f
    }

    abstract fun drawProgressImage(g: Graphics2D, startY: Int)

    companion object {

        val EYE_COLOR = Color(172, 50, 50)
        fun drawSplitText(
            g: Graphics2D,
            desc: String,
            value: String,
            x: Int,
            y: Int,
            width: Int,
            font: Font? = RUNESCAPE_FONT.getFont()?.deriveFont(15.0f),
            color: Color = Color.WHITE
        ) {
            val valueWidth = g.getFontMetrics(font).stringWidth(value)
            drawShadowedText(g, desc, x, y, false, color, font)
            drawShadowedText(g, value, width - valueWidth + x - 6, y, false, color, font)
        }

        fun drawTitle(g: Graphics2D, text: String, x: Int, y: Int) {
            val oldFont = g.font
            g.font = ATFont.HEIDH_FONT.getFont()?.deriveFont(50.0f)
            val width = g.getFontMetrics(g.font).stringWidth(text)
            val x = (x - width / 2.0).toInt()
            g.color = Color.WHITE
            g.drawString(text, x - 1, y - 1)
            g.drawString(text, x + 1, y + 1)
            g.drawString(text, x + 1, y - 1)
            g.drawString(text, x - 1, y + 1)
            g.color = Color.BLACK
            g.drawString(text, x, y)
            g.font = oldFont
        }

        fun Tile.drawOnMap(g: Graphics2D, color: Color = Color.GREEN) {
            val oldc = g.color
            val center = Rectangle(mapPoint().x, mapPoint().y, 3, 3)
            if (center.x > 0 && center.y > 0) {
                g.color = color
                g.draw(center)
            }
            g.color = oldc
        }

        fun Tile.drawCircleOnMap(g: Graphics2D, radius: Int) {
            val ellipse = Ellipse2D.Double()
            val centerPoint = mapPoint()
            val mmRadius = centerPoint.distance(Tile(x() + radius, y() + radius).mapPoint())
            val corner = Point(centerPoint.x + mmRadius.toInt(), centerPoint.y + mmRadius.toInt())
            ellipse.setFrameFromCenter(centerPoint, corner)
            g.draw(ellipse)
        }

        fun Tile.drawOnScreen(
            g: Graphics,
            text: String? = null,
            outlineColor: Color? = Color.GREEN,
            fillColor: Color? = null
        ) {
            val c = g.color
            val bounds = matrix(ClientContext.ctx()).bounds()
            if (bounds != null) {
                val p = bounds
                if (outlineColor != null) {
                    g.color = outlineColor
                    g.drawPolygon(p)
                }
                if (fillColor != null) {
                    g.color = fillColor
                    g.fillPolygon(p)
                }
                if (text != null)
                    g.drawString(text, bounds.bounds.centerX.toInt(), bounds.bounds.centerY.toInt())
            }
            g.color = c
        }

        fun Tile.drawCollisions(g: Graphics2D, flags: Array<IntArray>) {
            val bounds = matrix(ClientContext.ctx()).bounds() ?: return
            val flag = collisionFlag(flags)
            if (rockfallBlock(flags)) {
                drawOnScreen(g, flag.toString(), Color.LIGHT_GRAY)
            } else if (blocked(flags)) {
                drawOnScreen(g, flag.toString(), Color.RED)
            } else {
                g.drawString(flag.toString(), bounds.bounds.centerX.toInt(), bounds.bounds.centerY.toInt())
                val south = Line2D.Double(
                    Point(bounds.xpoints[0], bounds.ypoints[0]),
                    Point(bounds.xpoints[1], bounds.ypoints[1])
                )
                val east = Line2D.Double(
                    Point(bounds.xpoints[1], bounds.ypoints[1]),
                    Point(bounds.xpoints[2], bounds.ypoints[2])
                )
                val north = Line2D.Double(
                    Point(bounds.xpoints[2], bounds.ypoints[2]),
                    Point(bounds.xpoints[3], bounds.ypoints[3])
                )
                val west = Line2D.Double(
                    Point(bounds.xpoints[3], bounds.ypoints[3]),
                    Point(bounds.xpoints[0], bounds.ypoints[0])
                )
                g.color = if (flag and Flag.W_S == 0) Color.GREEN else Color.RED
                g.draw(south)

                g.color = if (flag and Flag.W_E == 0) Color.GREEN else Color.RED
                g.draw(east)

                g.color = if (flag and Flag.W_N == 0) Color.GREEN else Color.RED
                g.draw(north)

                g.color = if (flag and Flag.W_W == 0) Color.GREEN else Color.RED
                g.draw(west)
            }
        }

        fun drawShadowedText(
            g: Graphics2D,
            text: String,
            x: Int,
            y: Int,
            doubleShadow: Boolean = false,
            color: Color = Color.ORANGE,
            font: Font? = RUNESCAPE_FONT.getFont()?.deriveFont(15.0f)
        ) {
            val oldF = g.font
            g.font = font
            g.color = Color.BLACK
            if (doubleShadow) {
                g.drawString(text, x + 2, y - 2)
            }
            g.drawString(text, x + 1, y - 1)
            g.color = color
            g.drawString(text, x, y)
            g.font = oldF
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

        val SMALL20 = Font("Calibri", Font.BOLD, 25)
        val SMALL14 = Font("Calibri", Font.BOLD, 14)

        val DECIMAL_FORMAT = DecimalFormat("###,###,###.###")

        val WHITE = Color(-0x77000001, true)
        val LIGHT_BLUE = Color(0, 136, 255, 50)
        val LIGHT_GREEN = Color(89, 255, 11, 38)
        val RED_OPAQUE = Color(255, 64, 11, 50)
        val DARK_GREEN = Color(18, 107, 39, 92)
        val DARK_BLUE = Color(22, 72, 107, 151)
        val BLACK_B = Color(-0x34000000, true)
        val GRAY = Color.WHITE.darker()
        val RED_PALE = Color(0xFF453A)
        val GREEN_PALE = Color(0x75FF4F)
        val BLACK_A = Color(-0x45000000, true)
        val DARK_ORANGE = Color.ORANGE.darker()
        val DARK_RED = Color.RED.darker()
    }


}