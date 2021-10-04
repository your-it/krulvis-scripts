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
    val numbers = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 0)
    println("Odd: ${numbers.filter { it % 2 == 1 }.joinToString()}")
}