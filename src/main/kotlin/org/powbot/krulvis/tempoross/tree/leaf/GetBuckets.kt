package org.powbot.krulvis.tempoross.tree.leaf

import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Timer
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.tempoross.Tempoross

class GetBuckets(script: Tempoross) : Leaf<Tempoross>(script, "Getting water") {
    override fun execute() {
        var bucketCount = script.getBucketCount()
        val bucketCrate = script.getBucketCrate()
        val timer = Timer(5000)
        while (!timer.isFinished() && bucketCount < script.buckets) {
            if (walkAndInteract(bucketCrate, getBucketInteraction(bucketCount))) {
                waitFor {
                    bucketCount = script.getBucketCount()
                    bucketCount >= script.buckets
                }
            }
        }

    }

    private fun getBucketInteraction(bucketCount: Int): String {
        val required = script.buckets - bucketCount
        if (required >= 5) {
            return "Take-5"
        }
        return "Take-1"
    }


}