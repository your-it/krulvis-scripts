package org.powbot.krulvis.api.script

import com.google.common.eventbus.Subscribe
import org.powbot.api.Tile
import org.powbot.api.event.TickEvent
import org.powbot.api.rt4.Game
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Npc
import org.powbot.api.rt4.Projectile
import org.powbot.krulvis.api.ATContext.me

interface ProjectileEvader {
    val projectiles: MutableList<Projectile>
    var projectileSafespot: Tile

    @Subscribe
    fun onProjectileTickEvent(_e: TickEvent) {
        projectiles.forEach {
            if (Game.cycle() > it.cycleEnd) {
                projectiles.remove(it)
            }
        }
    }

    private fun findSafeSpotFromProjectile(target: Npc, melee: Boolean) {
        val dangerousTiles = projectiles.map { it.destination() }
        val targetTile = target.tile()
        val centerTile = if (melee) targetTile else me.tile()
        val distanceToTarget = targetTile.distance()
        val collisionMap = Movement.collisionMap(centerTile.floor).collisionMap.flags
        val grid = mutableListOf<Pair<Tile, Double>>()
        for (x in -2 until 2) {
            for (y in -2 until 2) {
                val t = Tile(centerTile.x + x, centerTile.y + y, centerTile.floor)
                if (t.blocked(collisionMap)) continue
                //If we are further than 5 tiles away, make sure that the tile is closer to the target so we don't walk further away
                if (distanceToTarget <= 5 || t.distanceTo(targetTile) < distanceToTarget) {
                    grid.add(t to dangerousTiles.minOf { it.distanceTo(t) })
                }
            }
        }
        projectileSafespot = grid.maxByOrNull { it.second }!!.first
    }
}