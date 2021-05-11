package org.powbot.krulvis.api.gui.equipment

import org.powbot.krulvis.api.extensions.items.EquipmentItem
import org.powbot.krulvis.api.utils.requirements.Requirement
import org.powbot.krulvis.api.extensions.items.Equipment
import org.powerbot.script.rt4.Equipment.Slot
import java.awt.Point
import java.awt.image.BufferedImage

enum class VisualSlot(var location: Point, var iSlot: Slot) {
    HEAD(Point(75, 13), Slot.HEAD),
    CAPE(Point(34, 50), Slot.CAPE),
    NECK(Point(75, 50), Slot.NECK),
    AMMO(Point(115, 50), Slot.QUIVER),
    CHEST(Point(75, 90), Slot.TORSO),
    WEAPON(Point(18, 90), Slot.MAIN_HAND),
    SHIELD(Point(130, 90), Slot.OFF_HAND),
    LEGS(Point(75, 130), Slot.LEGS),
    FEET(Point(75, 170), Slot.FEET),
    HANDS(Point(18, 170), Slot.HANDS),
    RING(Point(130, 170), Slot.RING);
}

fun List<org.powbot.krulvis.api.extensions.items.Equipment>.get(slot: Slot): EquipmentItem? = firstOrNull { it.slot == slot }

data class GearSet(val gear: List<Equipment>, val ammoCount: Int) {

    fun isEmpty(): Boolean = gear.isEmpty()

    fun getEquipmentItems(): MutableList<EquipmentItem> = gear.map { it as EquipmentItem }.toMutableList()

    fun getRequirements(): List<Requirement> = gear.flatMap { it.requirements }
}

fun List<Equipment>.contains(slot: VisualSlot): Boolean = any { it.slot == slot.iSlot }

fun List<Equipment>.getImage(slot: VisualSlot): BufferedImage? = firstOrNull { it.slot == slot.iSlot }?.image
