package org.powbot.krulvis.tithe

import org.powbot.krulvis.api.ATContext

import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.interact
import org.powerbot.script.rt4.GameObject

class Patch(var go: GameObject) {

//    val go: GameObject
//        get() = ATContext.ctx.objects.toStream().at(tile).filter { it.name().isNotEmpty() }.findFirst().get()

    fun refresh() {
        go = ATContext.ctx.objects.toStream().at(go.tile()).filter { it.name().isNotEmpty() }.findFirst().get()
    }

    val isNill get() = go == GameObject.NIL

    val id get() = go.id()

    fun isEmpty(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id == EMPTY
    }

    fun isDone(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in DONE
    }

    fun isPlanted(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in intArrayOf(*PLANTED, *WATERED_1, *GROWN_1, *WATERED_2, *GROWN_2, *WATERED_3, *DONE)
    }

    fun needsWatering(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in intArrayOf(*PLANTED, *GROWN_1, *GROWN_2)
    }

    fun blighted(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in BLIGHTED
    }


    fun needsAction(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in intArrayOf(EMPTY, *PLANTED, *GROWN_1, *GROWN_2, *DONE)
    }

    fun clear(): Boolean = interact(go, "Clear")

    fun water(): Boolean = interact(go, "Water")

    fun harvest(): Boolean = interact(go, "Harvest")

    fun plant(seed: Int): Boolean = interact(go, "Use", selectItem = seed)

    override fun toString(): String = "Patch(tile=${go.tile()})"

    companion object {
        val EMPTY = 27383
        val PLANTED = intArrayOf(27384, 27395)
        val WATERED_1 = intArrayOf(27385, 27396)
        val GROWN_1 = intArrayOf(27387, 27398)
        val WATERED_2 = intArrayOf(27388, 27399)
        val GROWN_2 = intArrayOf(27390, 27401)
        val WATERED_3 = intArrayOf(27391, 27402)
        val DONE = intArrayOf(27393, 27404)
        val BLIGHTED = intArrayOf(27386, 27389, 27392, 27394, 27397, 27400, 27403, 27405)

        fun GameObject.isPatch(): Boolean =
            id() in intArrayOf(
                EMPTY,
                *PLANTED,
                *WATERED_1,
                *GROWN_1,
                *WATERED_2,
                *GROWN_2,
                *WATERED_3,
                *DONE,
                *BLIGHTED
            )

        fun List<Patch>.hasEmpty(): Boolean = any { it.isEmpty() }

        fun List<Patch>.hasDone(): Boolean = any { it.isDone() }

        fun List<Patch>.nearest(): Patch = minByOrNull { it.go.distance() } ?: Patch(GameObject.NIL)

    }

}