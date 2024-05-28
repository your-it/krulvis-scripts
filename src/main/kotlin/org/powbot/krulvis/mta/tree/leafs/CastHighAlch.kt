package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mta.MTA
import org.powbot.krulvis.mta.rooms.AlchemyRoom
import kotlin.random.Random

class CastHighAlch(script: MTA) : Leaf<MTA>(script, "Casting high alch") {
	override fun execute() {
		val item = AlchemyRoom.bestItem.inventoryItem()
		val coins = Inventory.stream().id(995).count(true)
		if (castingDelay.isFinished()) {
			var invOpen = false
			if (Magic.Spell.HIGH_ALCHEMY.cast()) {
				script.log.info("Selected HA")
				invOpen = waitFor { Game.tab() == Game.Tab.INVENTORY }
			}
			script.log.info("Game.tab() = ${Game.tab()}, invOpen=$invOpen")
			if (invOpen && item.interact("Cast")) {
				castingDelay.reset(Random.nextInt(1800, 2400))
				waitFor { !casting() && coins < Inventory.stream().name("Coins").count(true) }
				script.log.info("Done casting HA")
			}
		}

	}

	var castingDelay = Timer(1500)

	private fun casting() = Magic.magicspell() == Magic.Spell.HIGH_ALCHEMY
}