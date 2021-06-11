package org.powbot.krulvis.tithe.tree.tree

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.Data.EMPTY_CAN
import org.powbot.krulvis.tithe.TitheFarmer
import org.powerbot.script.rt4.GameObject
import java.util.*

class Deposit(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Depositing") {

    fun getSack(): Optional<GameObject> =
        ctx.objects.toStream(25).name("Sack").nearest().findFirst()

    override fun execute() {
        val sack = getSack()
        println("Sack: $sack")
        sack.ifPresent {
            if (ATContext.interact(it, "Deposit")) {
                waitFor { ctx.inventory.toStream().id(*Data.HARVEST).isEmpty() }
            }
        }
    }
}