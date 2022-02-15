package org.powbot.krulvis.api.extensions.watcher

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Npc
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NpcDeathWatcher(private val npc: Npc, private val onDeath: () -> Unit) : Watcher() {

    val latch = CountDownLatch(1)

    init {
        GlobalScope.launch {
            latch.await(60, TimeUnit.SECONDS)
            unregister()
        }
    }

    @com.google.common.eventbus.Subscribe
    fun onTickEvent(_e: TickEvent) {
        if (npc.healthBarVisible() && npc.healthPercent() == 0) {
            onDeath()
            latch.countDown()
        } else if (!npc.valid()) {
            latch.countDown()
        }
    }
}