package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.script.tree.*
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.COOKED
import org.powbot.krulvis.tempoross.Data.DRAGON_HARPOON
import org.powbot.krulvis.tempoross.Data.RAW
import org.powbot.krulvis.tempoross.Tempoross
import org.powbot.krulvis.tempoross.tree.leaf.Fish
import org.powbot.krulvis.tempoross.tree.leaf.GetRope
import org.powbot.krulvis.tempoross.tree.leaf.Kill
import org.powbot.krulvis.tempoross.tree.leaf.Water
import org.powerbot.script.rt4.Npc

class ShouldSpec(script: Tempoross) : Branch<Tempoross>(script, "Should Spec") {
    override fun validate(): Boolean {
        return ctx.combat.specialPercentage() == 100
                && ctx.equipment.toStream().id(DRAGON_HARPOON).isNotEmpty()
                && (script.lastLeaf is Fish || script.lastLeaf is Kill)
    }


    override val successComponent: TreeComponent<Tempoross> = SimpleLeaf(script, "Special Attack") {
        if (ctx.combat.specialAttack(true)) {
            waitFor { ctx.combat.specialPercentage() < 100 }
        }
    }
    override val failedComponent: TreeComponent<Tempoross> = ShouldKill(script)
}

class ShouldKill(script: Tempoross) : Branch<Tempoross>(script, "Should Kill") {
    override fun validate(): Boolean {
        //Make sure that we keep shoot the leftovers
        val count = ctx.inventory.toStream().id(RAW, COOKED).count()
        val hp = script.getHealth()
        if (((count >= 1 && hp <= 5) || count > hp) && atAmmoCrate()) {
            return false
        }
        return script.canKill()
    }

    fun atAmmoCrate(): Boolean {
        val ammoCrate = script.getAmmoCrate()
        return ammoCrate.isPresent && ammoCrate.get().distance() <= 2
    }

    override val successComponent: TreeComponent<Tempoross> = Kill(script)
    override val failedComponent: TreeComponent<Tempoross> =
        SimpleBranch(script, "Should get rope", GetRope(script), ShouldGetWater(script)) {
            script.blockedTiles.clear()
            script.triedPaths.clear()
            script.detectDangerousTiles()

            !ctx.inventory.containsOneOf(ROPE)
        }
}

class ShouldGetWater(script: Tempoross) : Branch<Tempoross>(script, "Should get water") {
    override fun validate(): Boolean {
        if (ctx.inventory.containsOneOf(BUCKET_OF_WATER)) {
            return false
        }
        val bucketCrate = script.getBucketCrate()
        return bucketCrate.isPresent && bucketCrate.get().distance() <= 5
    }

    override val successComponent: TreeComponent<Tempoross> = Water(script)
    override val failedComponent: TreeComponent<Tempoross> = ShouldShoot(script)
}