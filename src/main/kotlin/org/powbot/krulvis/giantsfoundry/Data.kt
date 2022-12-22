package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Varpbits

val VARP = 3429

enum class BonusType {
    Broad,
    Flat,
    Heavy,
    Light,
    Narrow,
    Spiked;

    companion object {
        fun isBonus(component: Component) = values().any { it.name == component.text() }
        fun forComp(component: Component) = values().firstOrNull { it.name == component.text() }
        fun forText(text: String) = values().first { it.name == text }
    }
}

enum class MouldType(val shiftAmount: Int, val openPageBit: Int) {
    Forte(8, 0),
    Blades(13, 1),
    Tips(18, 2);

    fun selected() = Varpbits.varpbit(VARP, shiftAmount, optionsMask)

    fun hasSelected() = selected() > 0

    fun open() = Varpbits.varpbit(VARP, 6, 3) == openPageBit

    companion object {
        const val optionsMask = 31

        fun openPage(): MouldType {
            val varp = Varpbits.varpbit(VARP, 6, 3)
            return values().first { it.openPageBit == varp }
        }

        fun selectedAll(): Boolean {
            val varp = Varpbits.varpbit(VARP)
            return values().all {
                varp.shr(it.shiftAmount).and(optionsMask) > 0
            }
        }
    }
}