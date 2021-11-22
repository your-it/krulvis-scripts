package org.powbot.krulvis.test

open class Item {

    open fun interact(): Boolean = true

}

class InventoryItem() : Item() {

    fun getItem(): List<InventoryItem> {
        return listOf(InventoryItem())
    }

    override fun interact(): Boolean {
        return false
    }
}

fun main() {
    for (y in 0..9 step 3) {
        println(y)
    }
}