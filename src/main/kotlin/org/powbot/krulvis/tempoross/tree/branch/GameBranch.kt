package org.powbot.krulvis.tempoross.tree.branch

import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.ROPE
import org.powbot.krulvis.api.script.tree.Branch
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.script.tree.TreeComponent
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

class ShouldSpec(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        return combat.specialPercentage() == 100
                && equipment.toStream().id(DRAGON_HARPOON).isNotEmpty()
                && (script.lastLeaf is Fish || script.lastLeaf is Kill)
    }

    override fun onSuccess(): TreeComponent {
        return object : Leaf<Tempoross>(script, "Special Attack") {
            override fun loop() {
                if (combat.specialAttack(true)) {
                    waitFor { combat.specialPercentage() < 100 }
                }
            }
        }
    }

    override fun onFailure(): TreeComponent {
        return ShouldKill(script)
    }
}

class ShouldKill(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        //Make sure that we keep shoot the leftovers
        val count = inventory.toStream().id(RAW, COOKED).count()
        val hp = script.getHealth()
        if (((count >= 1 && hp <= 5) || count > hp) && atAmmoCrate()) {
            return false
        }
        return script.canKill()
    }

    override fun onSuccess(): TreeComponent {
        return Kill(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldGetRope(script)
    }

    fun atAmmoCrate(): Boolean {
        val ammoCrate = script.getAmmoCrate()
        return ammoCrate.isPresent && ammoCrate.get().distance() <= 2
    }
}

class ShouldGetRope(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        script.blockedTiles.clear()
        script.triedPaths.clear()

        script.detectDangerousTiles()

        return !inventory.contains(ROPE)
    }

    override fun onSuccess(): TreeComponent {
        return GetRope(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldGetWater(script)
    }
}

class ShouldGetWater(script: Tempoross) : Branch<Tempoross>(script) {
    override fun validate(): Boolean {
        if (inventory.contains(BUCKET_OF_WATER)) {
            return false
        }
        val bucketCrate = script.getBucketCrate()
        return bucketCrate.isPresent && bucketCrate.get().distance() <= 5
    }

    override fun onSuccess(): TreeComponent {
        return Water(script)
    }

    override fun onFailure(): TreeComponent {
        return ShouldShoot(script)
    }
}