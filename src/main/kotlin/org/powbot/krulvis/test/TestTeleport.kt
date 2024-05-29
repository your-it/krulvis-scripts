package org.powbot.krulvis.test

import org.powbot.api.Tile
import org.powbot.api.event.GameActionEvent
import org.powbot.api.event.GameObjectActionEvent
import org.powbot.api.event.MessageEvent
import org.powbot.api.rt4.Component
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.walking.local.LocalPath
import org.powbot.api.rt4.walking.model.Edge
import org.powbot.api.script.AbstractScript
import org.powbot.api.script.ScriptManifest
import org.powbot.krulvis.api.teleports.poh.openable.NexusPortalTeleport
import kotlin.system.measureTimeMillis

@ScriptManifest(name = "Krul test Teleport", version = "1.0.1", description = "", priv = true)
class TeleportScript : AbstractScript() {

	var teleport = NexusPortalTeleport.Falador

	@com.google.common.eventbus.Subscribe
	fun onGameActionEvent(e: GameActionEvent) {
		logger.info("$e")
	}

	@com.google.common.eventbus.Subscribe
	fun onMsg(e: MessageEvent) {
		logger.info("MSG: \n Type=${e.type}, msg=${e.message}")
	}

	override fun poll() {
		teleport.execute()
	}

}

fun main() {
	TeleportScript().startScript("127.0.0.1", "GIM", true)
}
