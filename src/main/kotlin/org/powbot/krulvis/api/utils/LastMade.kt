package org.powbot.krulvis.api.utils

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.getCount

object LastMade {

    /**
     * Map to keep track of when an item was last made.
     */
    val lastMadeTrackers = mutableListOf<LastMadeTracker>()

    /**
     * @return true if last made is longer than [time] milliseconds ago
     */
    fun stoppedMaking(id: Int, time: Long = 4000): Boolean {
        val lastMadeTracker = lastMadeTrackers.getForId(id)
        return lastMadeTracker.stoppedMaking(time)
    }

    /**
     * Add last made timer
     */
    fun track(id: Int) {
        lastMadeTrackers.getForId(id).stoppedMaking()
    }

    fun List<LastMadeTracker>.contains(id: Int): Boolean {
        return any { it.id == id }
    }

    fun List<LastMadeTracker>.getForId(id: Int): LastMadeTracker {
        if (!contains(id)) {
            lastMadeTrackers.add(LastMadeTracker(id))
        }
        return lastMadeTrackers.first { it.id == id }
    }

    class LastMadeTracker(val id: Int, var lastMadeAmount: Int, var lastMadeTime: Long) {

        fun getCurrentAmount(): Int {
            return Inventory.getCount(true, id)
        }

        constructor(id: Int) : this(id, Inventory.getCount(true, id), 0)

        fun stoppedMaking(time: Long = 4000): Boolean {
            val amount = getCurrentAmount()
            if (amount > lastMadeAmount) {
                lastMadeAmount = amount
                lastMadeTime = System.currentTimeMillis()
                return false
            } else if (System.currentTimeMillis() > lastMadeTime + time) {
                lastMadeAmount = amount
                return true
            }
            return false
        }
    }
}