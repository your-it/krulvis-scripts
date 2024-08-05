package org.powbot.krulvis.fighter.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.fighter.Fighter

class DodgeProjectile(script: Fighter) : Leaf<Fighter>(script, "DodgeProjectile") {

    override fun execute() {
        val dangerousTiles = script.projectiles.map { it.destination() }
        val myTile = me.tile()
        val collisionMap = Movement.collisionMap(myTile.floor).collisionMap.flags
        val grid = mutableListOf<Pair<Tile, Double>>()
        for (x in -2 until 2) {
            for (y in -2 until 2) {
                val t = Tile(x, y)
                if (!t.blocked(collisionMap))
                    grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
            }
        }
        script.projectileSafespot = grid.maxByOrNull { it.second }!!.first
        Movement.step(script.projectileSafespot, 0)
    }

}