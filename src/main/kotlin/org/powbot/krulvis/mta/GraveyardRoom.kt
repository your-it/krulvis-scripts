package org.powbot.krulvis.mta

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.Objects

object GraveyardRoom {
    const val GRAVEYARD_METHOD = "Graveyard"
    val spells = listOf(Magic.Spell.BONES_TO_PEACHES, Magic.Spell.BONES_TO_BANANAS)

    fun getSpell() = spells.firstOrNull { it.canCast() } ?: spells[1]

    fun getEdible(): Item = Inventory.stream().name(*Edible.values().map { it.name }.toTypedArray()).first()

    fun getBonesPile() = Objects.stream().name("Bones").action("Grab").nearest().first()

    fun Item.healing(): Int = Edible.values().firstOrNull { it.name == name() }?.healing ?: 0
    fun getBoneItems(): List<Item> = Inventory.stream().name("Animals' bones").toList()

    fun List<Item>.getFruitCount() = sumOf { it.getPoints() }

    private fun Item.getPoints(): Int = Bone.values().firstOrNull { it.id == id }?.points ?: 0

    enum class Bone(val id: Int) {
        Small(6904),
        Medium(6905),
        Large(6906),
        Giant(6907),
        ;

        val points = ordinal + 1
    }

    enum class Edible(val healing: Int) {
        Banana(2),
        Peach(8),
        ;
    }
}