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

class Refill(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Refilling") {

    fun getWaterBarrel(): Optional<GameObject> =
        ctx.objects.toStream(25).name("Water Barrel").nearest().findFirst()

    override fun execute() {
        val waterBarrel = getWaterBarrel()
        waterBarrel.ifPresent {
            if (it.distance() < 2 && ATContext.me.animation() != -1) {
                println("Already filling water...")
                waitFor(long()) { ctx.inventory.toStream().noneMatch { item -> item.id() in Data.WATER_CANS } }
            } else if (ATContext.interact(it, "Use", selectItem = EMPTY_CAN)) {
                waitFor { ATContext.me.facingTile() == it.tile() }
            }
        }
    }
}