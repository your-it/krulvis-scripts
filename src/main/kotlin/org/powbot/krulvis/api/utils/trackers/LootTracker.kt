package org.powbot.krulvis.api.utils.trackers

import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.script.ATScript
import org.powbot.krulvis.api.utils.Prices
import org.powbot.krulvis.api.utils.Timer
import org.powerbot.script.rt4.CacheItemConfig
import java.awt.Graphics2D
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
        g: Graphics2D,
        x: Int,
        y: Int,
        timer: Timer = script.timer,
        onlyTotal: Boolean = false
    ): Int {
        val yy = 11
        var y = y
        var totalWorth = 0
        val loots = this.loots
        loots.forEach {
            val worth = it.getWorth()
            totalWorth += worth
            val amount = formatAmount(it.amount)
            if (!onlyTotal) {
                script.painter.drawSplitText(
                    g,
                    "${it.name}: ",
                    "$amount (${formatAmount(timer.getPerHour(it.amount))}/hr)"
                            + (if (worth != it.amount) ", ${formatAmount(worth)} (${formatAmount(timer.getPerHour(worth))}/hr)" else ""),
                    x,
                    y
//                    font = g.font.deriveFont(it.fontSize.toFloat())
                )
                y += yy
            }
        }
        if (loots.size > 1) {
            y += 5
            script.painter.drawSplitText(
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
            val def = CacheItemConfig.load(ctx.bot().cacheWorker, id)
            price = Prices.getPrice(if (def.noted) id - 1 else id)
        }

        fun price(): Int {
            if (price == -1 && !getPriceThread.isAlive) {
                getPriceThread.start()
            }
            return price
        }

        val name = CacheItemConfig.load(ctx.bot().cacheWorker, id).name ?: ""

        var amount: Int = 0

        fun add(amount: Int) {
            this.amount += amount
        }

        fun getWorth(): Int = amount * price()
    }
}
