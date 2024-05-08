package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Prayer
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.runecrafting.Runecrafter

class MoveToBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Moving To Bank") {
    override fun execute() {
        val teleport = script.altar.bankTeleport
        Prayer.prayer(Prayer.Effect.PROTECT_FROM_MAGIC, false)
        if (ourianaUpstairsArea.contains(me)) {
            val ladder = ourianaLadder()
            if (ladder.distance() > 15) {
                ourianaPathToLadder.traverse(1)
            } else if (walkAndInteract(ladder, "Climb")) {
                waitFor { script.getBank().valid() }
            }
        } else if (teleport != null) {
            if (teleport.cast()) {
                waitFor(5000) { ourianaUpstairsArea.contains(me) || script.getBank().valid() || script.getAltar().valid() }
            }
        } else {
            Movement.moveToBank()
        }
    }

    val ourianaPathToLadder = listOf(Tile(2471, 3242, 0), Tile(2468, 3247, 0), Tile(2463, 3249, 0), Tile(2457, 3249, 0), Tile(2455, 3243, 0), Tile(2455, 3238, 0), Tile(2455, 3233, 0))

    val ourianaUpstairsArea = Area(Tile(2440, 3220), Tile(2486, 3255))
    fun ourianaLadder(): GameObject = Objects.stream().at(Tile(2452, 3231, 0)).name("Ladder").action("Climb").first()
}