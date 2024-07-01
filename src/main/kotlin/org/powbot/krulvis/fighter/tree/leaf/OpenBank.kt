package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Constants
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class OpenBank(script: Fighter) : Leaf<Fighter>(script, "Opening bank") {
	override fun execute() {
		val bankTeleport = script.bankTeleport
		if (Game.clientState() == Constants.GAME_LOGGED) {
			script.forcedBanking = true
			if (Prayer.quickPrayer()) {
				Prayer.quickPrayer(false)
			} else if (bankTeleport.execute()) {
				Bank.open()
			}
		}
	}
}