package org.powbot.krulvis.api.extensions.items

enum class Plank(override val ids: IntArray) : Item {
    NORMAL(intArrayOf(960)),
    OAK(intArrayOf(8778)),
    TEAK(intArrayOf(8780)),
    MAHOGANY(intArrayOf(8782));

    override fun hasWith(): Boolean {
        return getCount(false) >= 1
    }

    override fun getCount(countNoted: Boolean): Int {
        return getInventoryCount(countNoted)
    }
}