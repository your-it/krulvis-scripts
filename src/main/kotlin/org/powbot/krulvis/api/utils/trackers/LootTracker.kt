package org.powbot.krulvis.api.utils.trackers

import org.powbot.api.rt4.CacheItemConfig
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.script.painter.ATPainter
import org.powbot.krulvis.api.utils.Prices
import org.powbot.krulvis.api.utils.Timer
import org.powbot.mobile.drawing.Graphics
import org.powbot.mobile.script.ScriptManager
import java.lang.IllegalStateException
import java.util.logging.Logger
import kotlin.math.absoluteValue

class LootTracker(val script: ATScript) {

    val loots = mutableListOf<Loot>()

    fun contains(id: Int): Boolean = loots.any { it.id == id }

    fun addLoot(id: Int, amount: Int, fontSize: Int = 11) {
        if (loots.none { it.id == id }) {
            loots.add(Loot(id, fontSize))
        }
        val loot = loots.first { it.id == id }
        loot.add(amount)
    }

    fun reset() {
        loots.clear()
    }


    fun drawLoot(
        g: Graphics,
        x: Int,
        y: Int,
        timer: Timer = script.timer,
        onlyTotal: Boolean = false
    ): Int {
        var y = y
        var totalWorth = 0
        val loots = this.loots
        loots.forEach {
            val worth = it.getWorth()
            totalWorth += worth
            val amount = formatAmount(it.amount)
            if (!onlyTotal) {
                y = script.painter.drawSplitText(
                    g,
                    "${it.name}: ",
                    "$amount (${formatAmount(timer.getPerHour(it.amount))}/hr)",
                    x,
                    y
                )
                if (worth != it.amount) {
                    y = script.painter.drawSplitText(
                        g,
                        "Worth: ",
                        "${formatAmount(worth)} (${formatAmount(timer.getPerHour(worth))}/hr)",
                        x,
                        y
                    )
                }
            }
        }
        if (loots.size > 1) {
            y = script.painter.drawSplitText(
                g,
                "Total Loot: ",
                "${formatAmount(totalWorth)} (${formatAmount(timer.getPerHour(totalWorth))}/hr)",
                x,
                y
            )
        }
        return y
    }

    fun formatAmount(amount: Int): String {
        return when {
            (amount / 1000000).absoluteValue > 1 -> "%.1f".format(amount / 1000000.0) + "M"
            (amount / 1000).absoluteValue > 1 -> "%.1f".format(amount / 1000.0) + "K"
            else -> amount.toString()
        }
    }

    fun getLoot(id: Int): Loot? = loots.firstOrNull { it.id == id }

    /**
     * For ProgressUpdater
     */
    fun getProgress(): Map<String, Int> {
        return loots.map { Pair(it.name, it.amount) }.toMap()
    }


    fun getTotalWorth(): Int = loots.sumBy { it.getWorth() }

    class Loot(val id: Int, val fontSize: Int = 11) {

        private var price = -1

        private val getPriceThread = Thread {
            logger.info("Getting price for id=$id")
            val def = CacheItemConfig.load(id)
            price = Prices.getPrice(if (def.noted) id - 1 else id)
        }

        fun price(): Int {
            if (price == -1 && !getPriceThread.isAlive) {
                try {
                    getPriceThread.start()
                } catch (e: IllegalStateException) {
                    logger.warning("Found illegal state exception in Price getter")
                    e.printStackTrace()
                }
            }
            return price
        }

        val name = CacheItemConfig.load(id).name ?: ""

        var amount: Int = 0

        fun add(amount: Int) {
            this.amount += amount
        }

        fun getWorth(): Int = amount * price()
    }

    companion object {
        val logger = Logger.getLogger("LootTracker")
    }
}
