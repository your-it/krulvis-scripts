package org.powbot.krulvis.giantsfoundry

import org.powbot.api.rt4.Component
import org.powbot.api.rt4.Varpbits

val JOB_VARP = 3429
val HEAT_VARP = 3433

val ROOT = 754
fun currentTemp(): Int = Varpbits.varpbit(HEAT_VARP, 1023)

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

    fun selected() = Varpbits.varpbit(JOB_VARP, shiftAmount, optionsMask)

    fun hasSelected() = selected() > 0

    fun open() = Varpbits.varpbit(JOB_VARP, 6, 3) == openPageBit

    companion object {
        const val optionsMask = 31

        fun openPage(): MouldType {
            val varp = Varpbits.varpbit(JOB_VARP, 6, 3)
            return values().first { it.openPageBit == varp }
        }

        fun selectedAll(): Boolean {
            val varp = Varpbits.varpbit(JOB_VARP)
            return values().all {
                varp.shr(it.shiftAmount).and(optionsMask) > 0
            }
        }
    }
}