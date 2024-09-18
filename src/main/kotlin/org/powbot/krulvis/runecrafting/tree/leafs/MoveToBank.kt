package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.Area
import org.powbot.api.Tile
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.House
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.ouraniaPrayerAltarPath

class MoveToBank(script: Runecrafter) : Leaf<Runecrafter>(script, "Moving To Bank") {
    override fun execute() {
        val teleport = script.bankTeleport
        if (ouraniaUpstairsArea.contains(me)) {
            val ladder = ouraniaLadder()
            if (ladder.distance() > 15) {
                ouraniaPrayerAltarPath.traverse(1)
            } else if (walkAndInteract(ladder, "Climb")) {
                waitFor(5000) { script.getBank().valid() }
            }
        } else if (!teleport.executed) {
            if (teleport.execute())
                waitFor(5000) {
                    House.isInside() || ouraniaUpstairsArea.contains(me) || script.getBank()
                        .valid() || script.findChaosAltar().valid()
                }
        } else {
            Movement.moveToBank()
        }
    }


    val ouraniaUpstairsArea = Area(Tile(2440, 3220), Tile(2486, 3255))
    fun ouraniaLadder(): GameObject = Objects.stream().at(Tile(2452, 3231, 0)).name("Ladder").action("Climb").first()
}