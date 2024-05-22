package org.powbot.krulvis.mta.tree.leafs

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Magic
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.mta.AlchemyRoom
import org.powbot.krulvis.mta.MTA

class CastHighAlch(script: MTA) : Leaf<MTA>(script, "Casting high alch") {
    override fun execute() {
        val item = Inventory.stream().name(AlchemyRoom.bestItemName).firstOrNull() ?: return
        if (!casting()) {
            if (Magic.Spell.HIGH_ALCHEMY.cast()) {
                waitFor { casting() }
            }
        }

        val coins = Inventory.stream().id(995).count(true)
        if (casting() && item.interact("cast")) {
            waitFor { coins < Inventory.stream().name("Coins").count(true) }
            sleep(1200)
        }

    }

    private fun casting() = Magic.magicspell() == Magic.Spell.HIGH_ALCHEMY
}