package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.Production.stoppedMaking
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.tithe.Data.EMPTY_CAN
import org.powbot.krulvis.tithe.Data.WATER_CAN_FULL
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.*

class Refill(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Refilling") {

    fun getWaterBarrel(): Optional<GameObject> =
        Objects.stream(25).name("Water Barrel").nearest().findFirst()

    override fun execute() {
        val waterBarrel = getWaterBarrel()
        println("Barrel: present=${waterBarrel.isPresent}")
        val emptyCan = Inventory.stream().firstOrNull { it.id in EMPTY_CAN until WATER_CAN_FULL }
        waterBarrel.ifPresent {
            if (!stoppedMaking(WATER_CAN_FULL)) {
                println("Already filling water...")
                waitFor(long()) { Inventory.stream().noneMatch { item -> item.id() in EMPTY_CAN until WATER_CAN_FULL } }
            } else if (Game.tab(Game.Tab.INVENTORY) && walkAndInteract(
                    it,
                    "Use",
                    selectItem = emptyCan?.id ?: -1,
                    useMenu = false
                )
            ) {
                waitFor(5000) { !stoppedMaking(WATER_CAN_FULL) }
            }
        }
    }
}