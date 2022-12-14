package org.powbot.krulvis.api.extensions.watcher

import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.krulvis.api.ATContext.debug
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LootWatcher(private val tile: Tile, private val radius: Int = 4, private val isLoot: (GroundItem) -> Boolean) :
    Watcher() {

    private val latch = CountDownLatch(1)
    private val startLoot = groundItems()
    private val loot = mutableListOf<GroundItem>()

    private fun groundItems() = GroundItems.stream().within(tile, radius).list()

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        val groundItems = groundItems()
        val newItems = groundItems.filterNot { it in startLoot }
        if (newItems.isNotEmpty()) {
            val newLoot = newItems.filter(isLoot)
            debug("New groundItems found: [${newItems.joinToString { it.name() }}], loot: [${newLoot.joinToString { it.name() }}]")
            loot.addAll(newLoot)
            latch.countDown()
        }
    }

    fun waitForLoot(): List<GroundItem> {
        latch.await(5, TimeUnit.SECONDS)
        return loot
    }

}