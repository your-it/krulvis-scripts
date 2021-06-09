package org.powbot.krulvis.tithe

import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.ctx
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.short
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powerbot.script.Tile
import org.powerbot.script.rt4.Game
import org.powerbot.script.rt4.GameObject

class Patch(var go: GameObject, val tile: Tile, val index: Int) {

    constructor(go: GameObject, index: Int) : this(go, go.tile(), index)

    fun refresh() {
        go = ctx.objects.toStream(35).at(tile).filter { it.name().isNotEmpty() && it.name() != "null" }.findFirst()
            .get()
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
        return id in intArrayOf(*PLANTED, *GROWN_1, *GROWN_2, *DONE)
    }

    fun clear(): Boolean = interact("Clear")

    fun water(): Boolean = interact("Water")

    fun harvest(): Boolean = interact("Harvest")

    fun plant(seed: Int): Boolean {
        val selectedId = ctx.inventory.selectedItem().id()
        if (selectedId != seed) {
            ctx.game.tab(Game.Tab.INVENTORY)
            ctx.inventory.toStream().id(seed).findFirst().ifPresent {
                it.interact("Use")
            }
        }
        return waitFor(Utils.short()) { ctx.inventory.selectedItem().id() == seed } && go.click()
    }

    fun walkBetween(patches: List<Patch>): Boolean {
        if (!go.inViewport() || go.distance() > 12) {
            val minX = patches.minOf { tile.x() } + 2
            val t = tile
            val tile = Tile(minX, t.y(), 0)
            if (tile.matrix(ctx).onMap()) {
                ctx.movement.step(tile)
            } else {
                ATContext.walk(tile)
            }
            return false
        }
        return true
    }

    private fun interact(action: String): Boolean {
        val name = go.name()
        if (!ctx.client().isMenuOpen) {
            if (ctx.client().isMobile) {
                go.click()
            } else {
                go.click(false)
            }
        }
        if (waitFor(short()) { ctx.client().isMenuOpen }) {
            return ATContext.handleMenu(action, name)
        }
        return false
    }

    override fun toString(): String = "Patch(tile=${go.tile()})"

    companion object {
        val EMPTY = 27383
        val PLANTED = intArrayOf(27384, 27395)
        val WATERED_1 = PLANTED.map { it + 1 }.toIntArray()
        val GROWN_1 = PLANTED.map { it + 3 }.toIntArray()
        val WATERED_2 = PLANTED.map { it + 4 }.toIntArray()
        val GROWN_2 = PLANTED.map { it + 6 }.toIntArray()
        val WATERED_3 = PLANTED.map { it + 7 }.toIntArray()
        val DONE = PLANTED.map { it + 9 }.toIntArray()
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

        fun List<Patch>.nearest(): Patch = minByOrNull { it.tile.distance() } ?: Patch(GameObject.NIL, -1)

    }

}