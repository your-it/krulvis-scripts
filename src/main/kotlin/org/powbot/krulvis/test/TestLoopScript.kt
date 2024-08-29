package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Condition
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.mobile.drawing.Rendering
import org.powbot.mobile.rlib.generated.RMemory
import org.powbot.mobile.rlib.generated.RNpcs

@ScriptManifest(name = "Krul Test Loop", version = "1.0.1", description = "", priv = true)
class LoopScript : AbstractScript() {

	var collisionMap: Array<IntArray> = emptyArray()
	var npc: Npc = Npc.Nil
	var projectiles = emptyList<Projectile>()

	val STEPS = arrayOf("Select New", "Select Same", "Select New", "Select Same", "Select New", "Select Same")
	var step = 0

	var memCache = mapOf<Int, Int>()

	@Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
		if (e is NpcActionEvent) {
			npc = e.npc()
			logger.info("Interacted with npc=${npc.name()}")
			val values = readMemory(npc)
			val oldValues = memCache
			when (step) {
				0 -> memCache = values
				1, 3, 5 -> {
					memCache = values.filter { it.value == oldValues.getOrDefault(it.key, -1) }
					logger.info("Getting ${memCache.size} values that are the same")
				}

				2, 4, 6 -> {
					memCache =
						values.filter { it.key in oldValues.keys && it.value != oldValues.getOrDefault(it.key, -1) }
					logger.info("Values that are not the same: ${memCache.size}")
				}
			}
			if (step > 1) {
				memCache.forEach { (k, v) -> logger.info("$k: old=${oldValues[k]}, new=$v") }
			}
			step++
		}
	}

	@Subscribe
	fun onProject(e: ProjectileDestinationChangedEvent) {
		if (e.target() == Actor.Nil) {
			logger.info("ProjectileEvent id=${e.id}, destination=${e.destination()}")
		}
	}

	@Subscribe
	fun onTick(e: TickEvent) {
		val projectiles = Projectiles.stream().filtered { it.target() == Actor.Nil }.toList()
		val npc = Npcs.stream().name("Tormented demon").nearest().first()

		if (projectiles.isNotEmpty()) {
			logger.info("Found (${projectiles.size}) projectiles without target")
		}
	}


	fun readMemory(npc: Npc): Map<Int, Int> {
		logger.info("Reading memory")
		val rnpc = RNpcs.npc(npc.index()).get()
		val base = rnpc.baseAddrLong()
		val cb = RMemory.readLong(base + 1000, false)
		var x = 0
		val map = mutableMapOf<Int, Int>()
		while (x < 1000) {
//			val index = RMemory.readLong(cb + x, false)
			val value = RMemory.readInt(cb + x, false)
			map[x] = value
//			if (memCache.containsKey(x) && memCache[x] != value) {
//				val old = memCache.getOrDefault(x, -1)
//				logger.info("Mem[$x] old=${old}, new=${value}")
//			}
//			memCache[x] = value
			x += 4
		}
		return map
	}


	fun getMemValue(npc: Npc): Int {
		val rnpc = RNpcs.npc(npc.index()).get()
		val base = rnpc.baseAddrLong()
		val index = RMemory.readLong(base + 304, false)
		val value = RMemory.readInt(index, false)
		return value
	}

	fun getPrayerId(npc: Npc): Int {
		val a = 618603976
		val multiplier = 3716064

		val value = getMemValue(npc)
		return (value - -a) / multiplier
	}

	override fun poll() {
		val npc = npc
		if (npc.valid()) {
//			readMemory(npc)
			Condition.sleep(5000)
		}
	}

	fun getCannonballCount(): Int {
		val comp = Components.stream(651, 4).itemId(2).first()
		if (!comp.visible()) return 0
		val parent = comp.parent()
		if (parent.componentCount() <= comp.index() + 2) return 0
		return comp.parent().component(comp.index() + 2).text().toInt()
	}


	@Subscribe
	fun onRender(e: RenderEvent) {
		val g = Rendering
		val x = 20
		var y = 100
		g.drawString("Step=${STEPS[step]}", x, y)
		y += 15
		val npc = npc
		if (npc.valid()) {
			g.drawString("${npc.name}: id=${npc.id}, Anim=${npc.animation()}, Prayer=${getPrayerId(npc)}", x, y)
		}
		y += 15
	}

}

fun main() {
	LoopScript().startScript("127.0.0.1", "GIM", true)
}
