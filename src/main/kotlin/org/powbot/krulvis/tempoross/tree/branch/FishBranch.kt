package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.TreeComponent
import org.powbot.krulvis.tempoross.Data
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Cook
import org.powbot.krulvis.tempoross.tree.leaf.Fish
import org.powbot.krulvis.tempoross.tree.leaf.Shoot

class ShouldShoot(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {

        //Forced shooting happens after a tether attempt
        if (script.forcedShooting) {
            if (!inventory.contains(RAW, COOKED)) {
                script.forcedShooting = false
            }
            return script.forcedShooting
        }
        val ammoCrate = script.getAmmoCrate()
        if (inventory.contains(if (script.profile.cook) COOKED else RAW)
            && ammoCrate.isPresent && ammoCrate.get().distance() < 7
        ) {
            return true
        } else if (inventory.isFull && (!script.profile.cook || !inventory.contains(RAW))) {
            return true
        }
        val energy = script.getEnergy()
        val hp = script.getHealth()
        val fish = inventory.toStream().id(RAW, COOKED).count()
        val lowEnergy = energy / 2.5 < fish
        /* TODO optimize this calculation
            At low health you basically want to always shoot fish
            At higher health you need to make sure that there is enough energy left to shoot
         */
        return !inventory.contains(RAW) && inventory.contains(COOKED)
                || (hp <= 75 && lowEnergy && energy > 13)
    }

    override fun onSuccess(): TreeComponent {
        return Shoot(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldCook(script)
    }
}


class ShouldCook(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        val rawCount = inventory.getCount(RAW)
        script.collectFishSpots()
        script.bestFishSpot = script.getFishSpot(script.fishSpots)
        val doubleSpot = script.bestFishSpot.isPresent && script.bestFishSpot.get().id() == DOUBLE_FISH_ID
        val cookLocation = if (script.side == Tempoross.Side.NORTH) script.northCookSpot else script.cookLocation
        if (rawCount > 0 && !doubleSpot && cookLocation.distance() <= 1.5) {
            debug("Fishing because already there...")
            return true
        }
        val noEasyFishSpots =
            script.fishSpots.isEmpty() || script.fishSpots.all { script.containsDangerousTile(it.second) }

        if (rawCount > 3 && noEasyFishSpots && !script.hasDangerousPath(cookLocation)) {
            debug("Start early cooking since there is no available spot to fish at!")
            return true
        }
        val energy = script.getEnergy()
        val lowEnergy = energy / 4 < inventory.getCount(true, RAW, COOKED)
        val fullHealth = script.getHealth() == 100
        return script.profile.cook && rawCount > 0
                && (inventory.isFull || (lowEnergy && !fullHealth) || script.bestFishSpot.isEmpty)
    }

    override fun onSuccess(): TreeComponent {
        return Cook(script)
    }

    override fun onFailure(): TreeComponent {
        return Fish(script)
    }
}