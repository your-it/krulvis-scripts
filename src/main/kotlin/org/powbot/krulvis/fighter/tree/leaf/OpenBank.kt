package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.fighter.Fighter

class OpenBank(script: Fighter) : Leaf<Fighter>(script, "Opening bank") {
    override fun execute() {
        if (Game.clientState() == Constants.GAME_LOGGED) {
            script.forcedBanking = true
            if (Prayer.quickPrayer()) {
                Prayer.quickPrayer(false)
            }else if(script.bank){

            }
            script.bank.open()
        }
    }
}