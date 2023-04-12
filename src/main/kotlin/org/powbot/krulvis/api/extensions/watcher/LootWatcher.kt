package org.powbot.krulvis.api.extensions.watcher

import kotlinx.coroutines.Job
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.GroundItem
import org.powbot.api.rt4.GroundItems
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.getPrice
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class LootWatcher(private val tile: Tile, private val ammo: Int, private val radius: Int = 4, private val isLoot: (GroundItem) -> Boolean) :
        Watcher() {

    private val latch = CountDownLatch(1)
    private val startLoot = groundItems()
    private val loot = mutableListOf<GroundItem>()

    private fun groundItems() = GroundItems.stream().within(tile, radius).list()
    private var job: Job? = null

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        val groundItems = groundItems()
        val newItems = groundItems.filterNot { it in startLoot }
        if (newItems.count { it.id() != ammo } > 0) {
            val newLoot = newItems.filter(isLoot)
            debug("New groundItems found: [${newItems.joinToString { it.name() + ": " + it.getPrice() * it.stackSize() }}], loot: [${newLoot.joinToString { it.name() }}]")
            loot.addAll(newLoot)
            latch.countDown()
        }
    }

    fun waitForLoot(): List<GroundItem> {
        latch.await(10, TimeUnit.SECONDS)
        return loot
    }

}