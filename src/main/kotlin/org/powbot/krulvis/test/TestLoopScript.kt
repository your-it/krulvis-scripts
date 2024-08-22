package org.powbot.krulvis.test

import com.google.common.eventbus.Subscribe
import org.powbot.api.Color
import org.powbot.api.Tile
import org.powbot.api.event.*
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.mta.rooms.TelekineticRoom
import org.powbot.mobile.drawing.Rendering

@ScriptManifest(name = "Krul Test Loop", version = "1.0.1", description = "", priv = true)
class LoopScript : AbstractScript() {

	var origin = Tile(3212, 3216, 0) //varrock mine

	var collisionMap: Array<IntArray> = emptyArray()

	//    val dest = Tile(3253, 3420, 0) //Varrock bank
	var newDest = Tile(3231, 3207, 0)
	var localPath: LocalPath = LocalPath(emptyList())
	var comp: Component? = null
	val rocks by lazy { getOption<List<GameObjectActionEvent>>("rocks") }
	var path = emptyList<Edge<*>?>()
	var obj: GameObject? = null
	var npc: Npc? = null
	var lastLoop = System.currentTimeMillis()

	//Tile(x=3635, y=3362, floor=0)
	//Tile(x=3633, y=3359, floor=0)

	@com.google.common.eventbus.Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
	}

	@com.google.common.eventbus.Subscribe
	fun onMsg(e: MessageEvent) {
		logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
	}

	@Subscribe
	fun onProject(e: ProjectileDestinationChangedEvent) {
		logger.info("ProjectileEvent id=${e.id}, tile=${e.target()}, destination=${e.destination()}")
	}

	@Subscribe
	fun onTick(e: TickEvent) {
		val tile = me.tile()
		val projectile = Projectiles.stream().filtered { it.tile() == tile }.first()
		if (projectile.valid()) {
			logger.info("Found projectile going towards me")
			logger.info("Projectile: id=${projectile.id}")
		}
	}

	override fun onStart() {
		super.onStart()
		logger.info("Mysterious runes = ${Objects.stream().name("Mysterious ruins").first()}")
	}

	var mine = GameObject.Nil
	var direction = TelekineticRoom.Direction.NORTH
	override fun poll() {

		mine = Objects.stream().name("Daeyalt Essence").nearest().first()
		direction = mine.tile.getDirection()
		lastLoop = System.currentTimeMillis()
	}

	private fun Tile.getDirection(): TelekineticRoom.Direction {
		val me = me.tile()
		val dx = me.x - x()
		val dy = me.y - y()
		return if (dx >= 2) {
			TelekineticRoom.Direction.WEST
		} else if (dx <= -2) {
			TelekineticRoom.Direction.EAST
		} else if (dy >= 2) {
			TelekineticRoom.Direction.NORTH
		} else {
			TelekineticRoom.Direction.SOUTH
		}
	}

	@Subscribe
	fun onRender(e: RenderEvent) {
		val g = Rendering
		val x = 10
		var y = 20
		g.drawString("Orientation=${me.orientation()}, direction=${direction}", x, y)
		y += 15
		mine.tile.drawOnScreen(outlineColor = Color.RED)
	}

}

fun main() {
	LoopScript().startScript("127.0.0.1", "GIM", true)
}
