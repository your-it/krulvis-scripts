package org.powbot.krulvis.tithe

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.short
import org.powbot.krulvis.api.utils.Utils.waitFor

class Patch(var go: GameObject, val tile: Tile, val index: Int) {

    constructor(go: GameObject, index: Int) : this(go, go.tile(), index)

    fun refresh(gameObject: GameObject? = null) {
        go = gameObject ?: Objects.stream(35).at(tile).filtered { it.name().isNotEmpty() && it.name() != "null" }
            .findFirst()
            .get()
    }

    val isNill get() = go == GameObject.Nil

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

    fun handle(patches: List<Patch>): Boolean {
        return when {
            needsWatering() -> {
                walkBetween("Water", patches) && water()
            }
            isDone() -> {
                walkBetween("Harvest", patches) && harvest()
            }
            else -> {
                false
            }
        }
    }

    fun needsWatering(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in PLANTED || id in GROWN_1 || id in GROWN_2
    }

    fun blighted(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return go.name().contains("Blighted", true)
    }


    fun needsAction(refresh: Boolean = false): Boolean {
        if (refresh) {
            refresh()
        }
        return id in PLANTED || id in GROWN_1 || id in GROWN_2 || id in DONE
    }

    fun clear(): Boolean = interact("Clear")

    fun water(): Boolean = interact("Water")

    fun harvest(): Boolean = interact("Harvest")

    fun plant(seed: Int): Boolean {
        val selectedId = Inventory.selectedItem().id()
        if (selectedId != seed) {
            Game.tab(Game.Tab.INVENTORY)
            Inventory.stream().id(seed).findFirst().ifPresent {
                it.interact("Use")
            }
        }
        return waitFor(short()) { Inventory.selectedItem().id() == seed } && go.interact("Use")
    }

    fun walkBetween(action: String, patches: List<Patch>): Boolean {
        if (rightMenuOpen(action)) return true
        if (!go.inViewport() || go.distance() > 6) {
            val minX = patches.minOf { tile.x() } + 2
            val t = tile
            val tile = Tile(minX, t.y(), 0)
            if (tile.matrix().onMap()) {
                Movement.step(tile)
            } else {
                ATContext.walk(tile)
            }
            return false
        }
        return true
    }

    private fun rightMenuOpen(action: String): Boolean =
        Menu.opened() && Menu.contains { it.action.equals(action, true) }

    private fun interact(action: String): Boolean {
        if (!Menu.opened()) {
            go.click()
        }
        if (waitFor(short()) { Menu.opened() }) {
            if (rightMenuOpen(action)) {
                val interaction = Menu.click(
                    Menu.filter(
                        action
                    )
                )
                ATContext.turnRunOn()
                return interaction
            } else {
                Menu.click(Menu.filter("Cancel"))
            }
        }
        return false
    }

    override fun toString(): String = "Patch(tile=${go.tile()})"

    companion object {
        val EMPTY = 27383
        val PLANTED = intArrayOf(27384, 27395, 27406)
        val WATERED_1 = PLANTED.map { it + 1 }.toIntArray()
        val GROWN_1 = PLANTED.map { it + 3 }.toIntArray()
        val WATERED_2 = PLANTED.map { it + 4 }.toIntArray()
        val GROWN_2 = PLANTED.map { it + 6 }.toIntArray()
        val WATERED_3 = PLANTED.map { it + 7 }.toIntArray()
        val DONE = PLANTED.map { it + 9 }.toIntArray()
        val BLIGHTED = intArrayOf(27386, 27389, 27392, 27394, 27397, 27400, 27403, 27405)

        fun GameObject.isPatch(): Boolean {
            val name = name()
            return listOf("Logavano", "Bologano", "Golovanova", "Tithe patch").any { it in name }
        }


        fun List<Patch>.hasEmpty(): Boolean = any { it.isEmpty() }

        fun List<Patch>.hasDone(): Boolean = any { it.isDone() }

        fun List<Patch>.nearest(): Patch = minByOrNull { it.tile.distance() } ?: Patch(GameObject.Nil, -1)

        fun List<Patch>.tiles(): List<Tile> = map { it.tile }

        fun List<Patch>.refresh(): List<Patch> {
            val tiles = tiles()
            val gameObjects =
                Objects.stream(35).filtered { it.name().isNotEmpty() && it.name() != "null" && it.tile() in tiles }
                    .list()
            return onEach { patch -> patch.refresh(gameObjects.firstOrNull { go -> go.tile() == patch.tile }) }
        }

    }

}