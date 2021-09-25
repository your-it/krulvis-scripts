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
    val items: List<Item> = InventoryItem().getItem()

    println(items.first().interact())
}