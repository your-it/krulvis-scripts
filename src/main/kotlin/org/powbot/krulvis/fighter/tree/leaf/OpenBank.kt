package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.fighter.Fighter

class OpenBank(script: Fighter) : Leaf<Fighter>(script, "Opening bank") {
    override fun execute() {
        val bankTeleport = script.bankTeleport
        if (Game.clientState() == Constants.GAME_LOGGED) {
            script.forcedBanking = true
            if (Prayer.quickPrayer()) {
                Prayer.quickPrayer(false)
            } else if (bankTeleport.execute()) {
                val bank = Bank.getBank()
                if (bank.valid()) {
                    if (walkAndInteract(bank, "Open")) {
                        waitForDistance(bank) { Bank.opened() }
                    }
                } else
                    Movement.moveToBank()
            }
        }
    }
}