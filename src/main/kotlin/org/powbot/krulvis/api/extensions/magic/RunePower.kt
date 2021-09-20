package org.powbot.krulvis.api.extensions.magic

import org.powbot.krulvis.api.extensions.items.Staff

enum class RunePower {
    AIR,
    WATER,
    FIRE,
    EARTH,
    MIND,
    BODY,
    COSMIC,
    CHAOS,
    NATURE,
    DEATH,
    ASTRAL,
    LAW,
    SOUL,
    BLOOD,
    WRATH;

    /**
     * @return true if is Air, Water, Eart, Fire (Basic elementals)
     */
    fun isElemental(): Boolean {
        return elementals.contains(this)
    }

    /**
     * @return Combination runes come last so don't worry about that
     */
    fun getFirstRune(): Rune {
        return Rune.getForPower(this)
    }

    /**
     * @return First staff found with runePowers
     */
    fun getStaff(): Staff? {
        return Staff.values().firstOrNull { it.runePowers.contains(this) }
    }

    companion object {
        private val elementals = arrayOf(AIR, WATER, EARTH, FIRE)
    }
}