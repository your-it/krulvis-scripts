package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.api.rt4.Combat
import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.Inventory
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.api.script.tree.*
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DRAGON_HARPOON
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Fish
import org.powbot.krulvis.tempoross.tree.leaf.GetRope
import org.powbot.krulvis.tempoross.tree.leaf.Kill
import org.powbot.krulvis.tempoross.tree.leaf.Water

class ShouldSpec(script: Tempoross) : Branch<Tempoross>(script, "Should Spec") {
    override fun validate(): Boolean {
        return Combat.specialPercentage() == 100
                && Equipment.stream().id(DRAGON_HARPOON).isNotEmpty()
                && (script.lastLeaf is Fish || script.lastLeaf is Kill)
    }


    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Special Attack") {
        if (Combat.specialAttack(true)) {
            waitFor { Combat.specialPercentage() < 100 }
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
        return script.canKill()
    }

    fun atAmmoCrate(): Boolean {
        val ammoCrate = script.getAmmoCrate()
        return (ammoCrate?.distance() ?: 3) <= 2
    }

    override val successComponent: TreeComponent<Tempoross> = Kill(script)
    override val failedComponent: TreeComponent<Tempoross> =
        SimpleBranch(script, "Should get rope", GetRope(script), ShouldGetWater(script)) {
            script.blockedTiles.clear()
            script.triedPaths.clear()
            script.detectDangerousTiles()

            !Inventory.containsOneOf(ROPE)
        }
}

class ShouldGetWater(script: Tempoross) : Branch<Tempoross>(script, "Should get water") {
    override fun validate(): Boolean {
        if (Inventory.containsOneOf(BUCKET_OF_WATER)) {
            return false
        }
        val bucketCrate = script.getBucketCrate()
        return (bucketCrate?.distance() ?: 6) <= 5
    }

    override val successComponent: TreeComponent<Tempoross> = Water(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldShoot(script)
}