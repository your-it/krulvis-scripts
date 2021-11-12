package org.powbot.krulvis.blastfurnace.tree.leaf

import org.powbot.api.rt4.*
import org.powbot.krulvis.api.ATContext
import org.powbot.krulvis.api.ATContext.containsOneOf
import org.powbot.krulvis.api.ATContext.interact
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.extensions.items.Item.Companion.BUCKET_OF_WATER
import org.powbot.krulvis.api.extensions.items.Item.Companion.EMPTY_BUCKET
import org.powbot.krulvis.api.extensions.items.Ore
import org.powbot.krulvis.api.utils.Random
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.sleep
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.blastfurnace.BlastFurnace
import org.powbot.krulvis.blastfurnace.COAL_BAG
import org.powbot.krulvis.blastfurnace.GOLD_GLOVES

class CoolDispenser(script: BlastFurnace) : Leaf<BlastFurnace>(script, "Cool dispenser") {


    override fun execute() {
        if (Inventory.containsOneOf(BUCKET_OF_WATER)) {
            val dispenserMatrix = script.dispenserTile.matrix()
            if ((Inventory.selectedItem().id == BUCKET_OF_WATER || Inventory.stream().id(BUCKET_OF_WATER).first()
                    .interact("Use")) && script.interact(dispenserMatrix, "Use")
            ) {
                waitFor(long()) { script.cooledDispenser() }
            } else {
                script.log.info("Failed to use bucket on dispenser")
            }
        } else if (Inventory.containsOneOf(EMPTY_BUCKET)) {
            val sink = Objects.stream().name("Sink").action("Fill-bucket").firstOrNull() ?: return
            if (sink.interact("Fill-bucket")) {
                waitFor(long()) { Inventory.containsOneOf(BUCKET_OF_WATER) }
            }
        } else {
            val bucket = GroundItems.stream().id(EMPTY_BUCKET).firstOrNull() ?: return
            if (bucket.interact("Take")) {
                waitFor(long()) { Inventory.containsOneOf(EMPTY_BUCKET) }
            }
        }

    }

}