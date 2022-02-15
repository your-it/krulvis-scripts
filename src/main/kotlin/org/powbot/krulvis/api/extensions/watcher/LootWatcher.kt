package org.powbot.krulvis.api.extensions.watcher

import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LootWatcher(private val tile: Tile, private val radius: Int = 3, private val isLoot: (GroundItem) -> Boolean) :
    Watcher() {

    private val latch = CountDownLatch(1)
    private val startLoot = groundItems()
    private val loot = mutableListOf<GroundItem>()

    private fun groundItems() = GroundItems.stream().within(tile, radius).list()

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        val groundItems = groundItems()
        if (groundItems != startLoot) {
            loot.addAll(groundItems.filterNot { it in startLoot }.filter(isLoot))
            latch.countDown()
        }
    }

    fun waitForLoot(): List<GroundItem> {
        latch.await(5, TimeUnit.SECONDS)
        return loot
    }

}