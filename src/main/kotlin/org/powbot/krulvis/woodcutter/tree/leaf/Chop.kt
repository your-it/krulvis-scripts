package org.powbot.krulvis.woodcutter.tree.leaf

import org.powbot.api.rt4.Bank
import org.powbot.api.rt4.Objects
import org.powbot.api.rt4.Players
import org.powbot.api.rt4.walking.local.Utils
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.distance
import org.powbot.krulvis.api.utils.Utils.long
import org.powbot.krulvis.api.utils.Utils.waitFor
import org.powbot.krulvis.woodcutter.Woodcutter

class Chop(script: Woodcutter) : Leaf<Woodcutter>(script, "Chop Tree") {
    override fun execute() {
        Bank.close()


        val trees = script.trees.map {
            Objects.stream().at(it).action("Chop down", "Cut").firstOrNull()
        }

        script.log.info("Trees: ${trees.map { "${it?.name}: ${it?.tile}" }.joinToString()}")

        val tree = trees.filterNotNull()
            .minByOrNull { it.distance() }
        val action = if (tree?.actions()?.contains("Chop down") == true) "Chop down" else "Cut"
        if (tree != null && Utils.walkAndInteract(tree, action) && waitFor(long()) {
                Players.local().animation() != -1
            }) {
            script.lastChopAnim = System.currentTimeMillis()
        }
    }
}