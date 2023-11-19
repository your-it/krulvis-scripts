package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.Random
import org.powbot.api.rt4.Inventory
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.WATER_ANIM
import org.powbot.krulvis.tempoross.Tempoross

class Water(script: Tempoross) : Leaf<Tempoross>(script, "Getting water") {
    override fun execute() {
        val hasBucket = Inventory.containsOneOf(EMPTY_BUCKET)
        if (hasBucket && me.animation() != WATER_ANIM) {
            val waterPump = script.getWaterpump()
            if (waterPump != null && walkAndInteract(waterPump, "Use")
                && waitFor(long()) { me.animation() == WATER_ANIM }
            ) {
                waitFor(long()) { !Inventory.containsOneOf(EMPTY_BUCKET) }
            }
        } else if (!hasBucket) {
            val bucketCrate = script.getBucketCrate()
            if (walkAndInteract(bucketCrate, "Take-5")) {
                waitFor(Random.nextInt(6500, 10000)) { Inventory.containsOneOf(EMPTY_BUCKET) }
            }
        }
    }

}