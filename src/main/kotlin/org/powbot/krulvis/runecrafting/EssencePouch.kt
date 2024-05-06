package org.powbot.krulvis.runecrafting

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.krulvis.api.utils.Utils.waitFor

enum class EssencePouch(val capacity: Int) {
    COLOSSAL(40),
    ;

    private var count: Int = 0
        set(value) {
            value.coerceIn(0, capacity)
            field = value
        }

    fun getCount() = count

    fun getItem(): Item? = Inventory.stream().nameContains(name).firstOrNull()

    fun filled(): Boolean = count >= capacity
    fun fill(): Boolean {
        val item = getItem() ?: return false
        val invEssence = essenceCount()
        if (item.interact("Fill")) {
            waitFor { invEssence != essenceCount() }
            count += invEssence - essenceCount()
        }
        return filled()
    }

    fun empty(): Boolean {
        if (Inventory.isFull()) return false
        val item = getItem() ?: return false
        val invEssence = essenceCount()
        if (item.interact("Empty")) {
            waitFor { invEssence < essenceCount() }
            val essenceCount = essenceCount()
            count -= essenceCount - invEssence
            return invEssence < essenceCount()
        }
        return false
    }

    fun shouldRepair(): Boolean = false

    companion object {
        fun essenceCount() = Inventory.stream().nameContains("essence").count().toInt()

        fun inInventory() = values().filter { it.getItem() != null }
    }
}