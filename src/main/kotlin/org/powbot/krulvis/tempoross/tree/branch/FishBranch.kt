package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.debug
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DOUBLE_FISH_ID
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Cook
import org.powbot.krulvis.tempoross.tree.leaf.Fish
import org.powbot.krulvis.tempoross.tree.leaf.Shoot

class ShouldShoot(script: Tempoross) : Branch<Tempoross>(script, "Should Shoot") {
    override fun validate(): Boolean {

        //Forced shooting happens after a tether attempt
        if (script.forcedShooting) {
            if (!Inventory.containsOneOf(RAW, COOKED)) {
                script.forcedShooting = false
            }
            return script.forcedShooting
        }
        val ammoCrate = script.getAmmoCrate()
        if (Inventory.containsOneOf(if (script.profile.cook) COOKED else RAW)
            && ammoCrate.isPresent && ammoCrate.get().distance() < 7
        ) {
            return true
        } else if (Inventory.isFull() && (!script.profile.cook || !Inventory.containsOneOf(RAW))) {
            return true
        }
        val energy = script.getEnergy()
        val hp = script.getHealth()
        val fish = Inventory.stream().id(RAW, COOKED).count()
        val lowEnergy = energy / 2.5 < fish
        /* TODO optimize this calculation
            At low health you basically want to always shoot fish
            At higher health you need to make sure that there is enough energy left to shoot
         */
        return !Inventory.containsOneOf(RAW) && Inventory.containsOneOf(COOKED)
                || (hp <= 75 && lowEnergy && energy > 13)
    }

    override val successComponent: TreeComponent<Tempoross> = Shoot(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldCook(script)
}


class ShouldCook(script: Tempoross) : Branch<Tempoross>(script, "Should Cook") {
    override fun validate(): Boolean {
        val rawCount = Inventory.getCount(RAW)
        script.collectFishSpots()
        script.bestFishSpot = script.getFishSpot(script.fishSpots)
        val doubleSpot = script.bestFishSpot.isPresent && script.bestFishSpot.get().id() == DOUBLE_FISH_ID
        val cookLocation = if (script.side == Tempoross.Side.NORTH) script.northCookSpot else script.cookLocation
        if (rawCount > 0 && !doubleSpot && cookLocation.distance() <= 1.5) {
            debug("Fishing because already there...")
            return true
        }
        val noEasyFishSpots =
            script.fishSpots.size == 0 || script.fishSpots.all { script.containsDangerousTile(it.second) }

        if (rawCount > 3 && noEasyFishSpots && !script.hasDangerousPath(cookLocation)) {
            debug("Start early cooking since there is no available spot to fish at!")
            return true
        }
        val energy = script.getEnergy()
        val lowEnergy = energy / 4 < Inventory.getCount(true, RAW, COOKED)
        val fullHealth = script.getHealth() == 100
        return script.profile.cook && rawCount > 0
                && (Inventory.isFull() || (lowEnergy && !fullHealth) || !script.bestFishSpot.isPresent)
    }

    override val successComponent: TreeComponent<Tempoross> = Cook(script)
    override val failedComponent: TreeComponent<Tempoross> = Fish(script)
}