package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Tile
import org.powbot.api.rt4.Movement
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.getWalkableNeighbor
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.Utils.long
import org.powbot.krulvis.api.extensions.Utils.waitFor
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.tempoross.Data.FILLING_ANIM
import org.powbot.krulvis.tempoross.Side
import org.powbot.krulvis.tempoross.Tempoross


class Shoot(script: Tempoross) : Leaf<Tempoross>(script, "Shooting") {
    override fun execute() {
        val ammo = script.getAmmoCrate()
        val dest = Movement.destination()
        val nearAmmoBox = ammo.distance() <= 3 || ammo.tile().distanceTo(dest) <= 3
        val shootingTile = getShootingTile(ammo.tile())
        val shooting = me.animation() == FILLING_ANIM && nearAmmoBox
        if (nearAmmoBox && script.burningTiles.contains(shootingTile)) {
            script.logger.info("Near ammo box, shooting but standing in burning tile")
            val safeTile = shootingTile.getWalkableNeighbor(
                false,
                checkForWalls = false,
                diagonalTiles = true
            ) { !script.burningTiles.contains(it) }
            if (safeTile == null) {
                script.logger.info("Can't find walkable safeTile :(")
            } else if (Movement.step(safeTile)) {
                waitForDistance(safeTile) { me.tile() == safeTile }
            }
            return
        }
        if (!ammo.valid()) {
            script.walkWhileDousing(script.side.anchorLocation, false)
        } else if (!shooting && script.interactWhileDousing(ammo, "Fill", script.side.mastLocation, false)) {
            waitFor(long()) { me.animation() == FILLING_ANIM }
        }
    }

    private fun getShootingTile(ammoCrate: Tile): Tile {
        return if (script.side == Side.NORTH) {
            ammoCrate.derive(-1, 0)
        } else {
            ammoCrate.derive(1, 0)
        }
    }

    private fun getSafeTile(): Tile {
        return if (script.side == Side.NORTH) {
            script.side.mastLocation.derive(2, 0)
        } else {
            script.side.mastLocation.derive(-2, 0)
        }
    }

}