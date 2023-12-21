package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.local.LocalPathFinder
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.HARPOON
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Data.SPEC_HARPOONS
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.*
import kotlin.math.roundToInt

class ShouldSpec(script: Tempoross) : Branch<Tempoross>(script, "Should Spec") {
    override fun validate(): Boolean {
        return script.spec &&
                Combat.specialPercentage() == 100
                && Equipment.stream().id(*SPEC_HARPOONS).isNotEmpty()
                && (script.lastLeaf is Fish || script.lastLeaf is Kill)
    }


    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Special Attack") {
        if (Combat.specialAttack(true)) {
            waitFor(5000) { Combat.specialPercentage() < 100 }
        }
    }
    override val failedComponent: TreeComponent<Tempoross> = ShouldDouse(script)
}


class ShouldDouse(script: Tempoross) :
    Branch<Tempoross>(script, "Should douse fire") {
    override fun validate(): Boolean {
        if (!Inventory.containsOneOf(BUCKET_OF_WATER)) {
            return false
        }
        fire = script.getNearestFire() ?: return false
        return if (script.solo) true else fire!!.distance() <= 2
    }


    var fire: Npc? = null

    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Douse") {
        if (fire!!.interact("Douse")) {
            waitFor(5000) { script.getNearestFire() != fire }
        }
    }
    override val failedComponent: TreeComponent<Tempoross> = ShouldKill(script)
}

class ShouldKill(script: Tempoross) : Branch<Tempoross>(script, "Should Kill") {
    override fun validate(): Boolean {
        //Make sure that we keep shoot the leftovers
        val count = Inventory.stream().id(RAW, COOKED).count()
        val hp = script.getHealth()
        if (((count >= 1 && hp <= 5) || count > hp) && atAmmoCrate()) {
            return false
        }
        return (!script.solo || !Inventory.containsOneOf(COOKED)) && script.canKill()
    }

    fun atAmmoCrate(): Boolean {
        val ammoCrate = script.getAmmoCrate()
        return (ammoCrate?.distance()?.roundToInt() ?: 3) <= 2
    }

    override val successComponent: TreeComponent<Tempoross> = Kill(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetWater(script)
}


class ShouldGetWater(script: Tempoross) :
    Branch<Tempoross>(script, "Should get water") {
    override fun validate(): Boolean {
        val filledBuckets = script.getFilledBuckets()
        if (filledBuckets >= script.buckets || script.getEnergy() <= 10) {
            return false
        } else if (script.getIntensity() == 0 && script.getHealth() == 100) {
            return true
        } else if (filledBuckets == 0 && script.getNearestFire() != null) {
            return true
        }
        return (script.getBucketCrate()?.distance()?.roundToInt() ?: 7) <= 6
    }

    override val successComponent: TreeComponent<Tempoross> = CanFillEmptyBucket(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetRope(script)
}

class CanFillEmptyBucket(script: Tempoross) :
    Branch<Tempoross>(script, "Can fill empty buckets") {
    override fun validate(): Boolean {
        return script.getEmptyBuckets() > 0
    }

    override val successComponent: TreeComponent<Tempoross> = FillBuckets(script)
    override val failedComponent: TreeComponent<Tempoross> = GetBuckets(script)
}

class ShouldGetRope(script: Tempoross) : Branch<Tempoross>(script, "Should get rope") {
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetHarpoon(script)
    override val successComponent: TreeComponent<Tempoross> = GetRope(script)

    override fun validate(): Boolean {
        script.burningTiles.clear()
        script.triedPaths.clear()
        script.detectDangerousTiles()

        return !script.hasOutfit && !Inventory.containsOneOf(ROPE)
    }
}

class ShouldGetHarpoon(script: Tempoross) : Branch<Tempoross>(script, "Should get harpoon") {
    override fun validate(): Boolean {
        Game.tab(Game.Tab.INVENTORY)
        return script.inventory.contains(HARPOON) && !Inventory.containsOneOf(HARPOON)
    }

    override val successComponent: TreeComponent<Tempoross> = GetHarpoon(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldGetHammer(script)
}

class ShouldGetHammer(script: Tempoross) : Branch<Tempoross>(script, "Should get hammer") {
    override val failedComponent: TreeComponent<Tempoross> = ShouldShoot(script)
    override val successComponent: TreeComponent<Tempoross> = GetHammer(script)

    override fun validate(): Boolean {
        return !script.hasHammer() && (script.getHammerContainer()?.distance()?.roundToInt() ?: 7) <= 6
    }
}




