package org.powbot.krulvis.tithe.tree.leaf

import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tithe.Data
import org.powbot.krulvis.tithe.TitheFarmer
import java.util.*

class Deposit(script: TitheFarmer) : Leaf<TitheFarmer>(script, "Depositing") {

    fun getSack(): Optional<GameObject> =
        Objects.stream(25).name("Sack").nearest().findFirst()

    override fun execute() {
        val sack = getSack()
        println("Sack: $sack")
        sack.ifPresent {
            if (ATContext.interact(it, "Deposit", useMenu = false)) {
                waitFor { Inventory.stream().id(*Data.HARVEST).isEmpty() }
            }
        }
    }
}