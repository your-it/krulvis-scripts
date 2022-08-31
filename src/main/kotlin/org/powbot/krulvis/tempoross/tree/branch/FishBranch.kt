package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.walking.local.LocalPathFinder
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

        val fish = Inventory.stream().id(RAW, COOKED).count().toInt()

        //Forced shooting happens after a tether attempt
        if (script.forcedShooting) {
            if (fish == 0) {
                script.forcedShooting = false
            }
            return script.forcedShooting
        }

        val energy = script.getEnergy()
        val hp = script.getHealth()
        val lowEnergy = energy / 2.5 < fish

        //If we are close to the ammo-box and have some fish, shoot em
        val ammoCrate = script.getAmmoCrate()
        if (Inventory.containsOneOf(if (script.cookFish) COOKED else RAW)
            && (ammoCrate?.distance().roundToInt() ?: 8) < 7
        ) {
            script.forcedShooting = true
            return true
        } else if (fish > 0 && (hp <= 75 && lowEnergy && energy > 13)) {
            script.forcedShooting = true
            return true
        } else if (Inventory.isFull() && (!script.cookFish || !Inventory.containsOneOf(RAW))) {
            return true
        }
        return false
    }

    override val successComponent: TreeComponent<Tempoross> = Shoot(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldCook(script)
}


class ShouldCook(script: Tempoross) : Branch<Tempoross>(script, "Should Cook") {
    override fun validate(): Boolean {
        val rawCount = Inventory.getCount(RAW)
        script.collectFishSpots()
        script.bestFishSpot = script.getFishSpot(script.fishSpots)

        if (!script.cookFish)
            return false

        val doubleSpot = script.bestFishSpot?.id() == DOUBLE_FISH_ID
        val cookLocation = script.side.cookLocation
        if (rawCount > 0 && !doubleSpot && cookLocation.distance() <= 1.5) {
            debug("Fishing because already there...")
            return true
        }
        val noEasyFishSpots =
            script.fishSpots.isEmpty() || script.fishSpots.all { script.containsDangerousTile(it.second) }

        if (rawCount > 3 && noEasyFishSpots && !script.hasDangerousPath(cookLocation)) {
            if (script.fishSpots.isEmpty() && script.side.totemLocation.distance() > 10) {
                script.log.info("Can't find cooking spots, walking to middle")
                val path = LocalPathFinder.findPath(script.side.totemLocation)
                if (path.isNotEmpty()) {
                    script.walkWhileDousing(path, true)
                } else {
                    Movement.step(script.side.totemLocation)
                }
                return false
            } else {
                script.log.info("Start early cooking since there is no available spot to fish at!")
                return true
            }
        }
        val energy = script.getEnergy()
        val lowEnergy = energy / 4 < Inventory.getCount(true, RAW, COOKED)
        val fullHealth = script.getHealth() == 100
        script.log.info("fullHealth=$fullHealth, energy=$energy, lowEnergy=$lowEnergy, rawCount=$rawCount")
        return rawCount > 0
                && (Inventory.isFull() || (lowEnergy && !fullHealth) || script.bestFishSpot == null)
    }

    override val successComponent: TreeComponent<Tempoross> = Cook(script)
    override val failedComponent: TreeComponent<Tempoross> = Fish(script)
}
