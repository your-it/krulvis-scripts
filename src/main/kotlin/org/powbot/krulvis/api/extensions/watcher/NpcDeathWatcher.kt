package org.powbot.krulvis.api.extensions.watcher

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Npc
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class NpcDeathWatcher(val npc: Npc, private val autoDestroyed: Boolean, private val onDeath: () -> Unit) : Watcher() {

	val logger = LoggerFactory.getLogger(javaClass.simpleName)
	val latch = CountDownLatch(1)

	val active get() = latch.count > 0

	init {
		GlobalScope.launch {
			awaitDeath()
			latch.countDown()
			unregister()
		}
	}

	fun awaitDeath() = latch.await(10, TimeUnit.SECONDS)

	@com.google.common.eventbus.Subscribe
	fun onTickEvent(_e: TickEvent) {
		if (npc.healthBarVisible() && npc.healthPercent() <= if (autoDestroyed) 8 else 0) {
//            debug("NpcWatcher found death=$npc")
			onDeath()
			latch.countDown()
		} else if (!npc.valid()) {
//            debug("NpcWatcher stopped=$npc")
			latch.countDown()
		} else {
//            debug("Watching NPC=${npc.name}, HP Visible=${npc.healthBarVisible()}, Percent=${npc.healthPercent()}")
		}
	}

}