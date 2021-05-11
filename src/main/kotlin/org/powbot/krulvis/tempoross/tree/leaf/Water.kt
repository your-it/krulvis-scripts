package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.script.tree.Leaf
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Data.WATER_ANIM
import org.powbot.krulvis.tempoross.Tempoross

class Water(script: Tempoross) : Leaf<Tempoross>(script, "Cooking") {
    override fun execute() {
        val hasBucket = ctx.inventory.containsOneOf(EMPTY_BUCKET)
        if (hasBucket && me.animation() != WATER_ANIM) {
            val waterPump = script.getWaterpump()
            if (waterPump.isPresent && interact(waterPump.get(), "Use")
                && waitFor(long()) { me.animation() == WATER_ANIM }
            ) {
                waitFor(long()) { !ctx.inventory.containsOneOf(EMPTY_BUCKET) }
            }
        } else if (!hasBucket) {
            val bucketCrate = script.getBucketCrate()
            if (bucketCrate.isPresent && interact(bucketCrate.get(), "Take-5")) {
                waitFor { ctx.inventory.containsOneOf(EMPTY_BUCKET) }
            }
        }
    }

}