package org.powbot.krulvis.runecrafting.tree.branches

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter

val RING_AREA = Area(Tile(3437, 9829, 0), Tile(3453, 9819, 0))
val RAT_AREA =
    Area(Tile(3457, 9823, 0), Tile(3459, 9807, 0), Tile(3469, 9810, 0), Tile(3476, 9817, 0), Tile(3468, 9824, 0))
val MAIN_CAVE_AREA =
    Area(Tile(3465, 9847, 0), Tile(3471, 9826, 0), Tile(3502, 9795, 0), Tile(3509, 9815, 0), Tile(3485, 9856, 0))
val LABORATORIES_AREA = Area(Tile(3509, 9827, 0), Tile(3646, 9667, 0))
val RUINS_AREA = Area(Tile(3572, 9761, 0), Tile(3542, 9785, 0))


class InRuinsArea(script: Runecrafter) : Branch<Runecrafter>(script, "InLaboratories?") {
    override val failedComponent: TreeComponent<Runecrafter> = InLaboratories(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "RuinsArea") {
        val ruins = Objects.stream(Tile(3561, 9781, 0)).name("Mysterious ruins").first()
        if (ruins.distance() > 10) {
            Movement.step(ruins)
        } else if (walkAndInteract(ruins, "Enter")) {
            waitForDistance(ruins) { script.altar.atAltar() }
        }
    }

    override fun validate(): Boolean {
        return RUINS_AREA.contains(me)
    }
}

class InLaboratories(script: Runecrafter) : Branch<Runecrafter>(script, "InLaboratories?") {
    override val failedComponent: TreeComponent<Runecrafter> = InMainCave(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "Laboratory") {
        val caveEntrance = Objects.stream(Tile(3540, 9772, 0)).name("Cave").first()
        if (caveEntrance.valid() && walkAndInteract(caveEntrance, "Enter")) {
            waitFor(long()) { RUINS_AREA.contains(me) }
        }
    }

    override fun validate(): Boolean {
        return LABORATORIES_AREA.contains(me)
    }
}

class InMainCave(script: Runecrafter) : Branch<Runecrafter>(script, "InRatArea?") {
    override val failedComponent: TreeComponent<Runecrafter> = InRatArea(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "RatArea") {
        val caveEntrance = Objects.stream(Tile(3500, 9803, 0)).name("Cave").first()
        if (caveEntrance.valid() && walkAndInteract(caveEntrance, "Enter")) {
            waitFor(long()) { me.tile() == Tile(3536, 9768, 0) }
        }
    }

    override fun validate(): Boolean {
        return MAIN_CAVE_AREA.contains(me)
    }
}

class InRatArea(script: Runecrafter) : Branch<Runecrafter>(script, "InRatArea?") {
    override val failedComponent: TreeComponent<Runecrafter> = InRingArea(script)
    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "RatArea") {
        val caveEntrance = Objects.stream(Tile(3467, 9820, 0)).name("Cave entrance").first()
        if (caveEntrance.valid() && walkAndInteract(caveEntrance, "Enter")) {
            waitFor(long()) { MAIN_CAVE_AREA.contains(me) }
        }
    }

    override fun validate(): Boolean {
        return RAT_AREA.contains(me)
    }
}

class InRingArea(script: Runecrafter) : Branch<Runecrafter>(script, "InRingArea?") {

    override val failedComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "TeleportToLabs") {
        if (script.altarTeleport.execute()) {
            waitFor(long()) { RING_AREA.contains(me) }
        }
    }

    override val successComponent: TreeComponent<Runecrafter> = SimpleLeaf(script, "RingArea") {
        val caveEntrance = Objects.stream(Tile(3447, 9821, 0)).name("Cave entrance").first()
        if (caveEntrance.valid() && walkAndInteract(caveEntrance, "Enter")) {
            waitFor(long()) { RAT_AREA.contains(me) }
        }
    }

    override fun validate(): Boolean {
        return RING_AREA.contains(me)
    }
}





