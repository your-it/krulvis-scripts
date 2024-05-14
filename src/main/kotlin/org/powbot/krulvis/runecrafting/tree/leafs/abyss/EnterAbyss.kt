package org.powbot.krulvis.runecrafting.tree.leafs.abyss

import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.utils.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.ABYSS
import org.powbot.krulvis.runecrafting.Runecrafter
import org.powbot.krulvis.runecrafting.tree.Abyss

class EnterAbyss(script: Runecrafter) : Leaf<Runecrafter>(script, "Entering Abyss") {
    override fun execute() {
        val mage = Npcs.stream().name("Mage of Zamorak").first()
        if (mage.distance() < 15 && mage.reachable()) {
            if (walkAndInteract(mage, "Teleport")) {
                waitForDistance(mage) { Abyss.inOuterCircle() }
            }
        } else {
            walkToMage()
        }
    }

    private fun walkToMage() {
        if (acrossDitch()) {
            if(Skills.level(Skill.Prayer) > 0 && Skills.realLevel(Skill.Prayer) > 21)
                Prayer.prayer(Prayer.Effect.PROTECT_ITEM, true)
            toMage.traverse(2, 8)
        } else {
            val ditch = Objects.stream(50).type(GameObject.Type.INTERACTIVE).name("Wilderness Ditch").action("Cross").nearest().first()
            if (ditch.distance() < 15) {
                if (walkAndInteract(ditch, "Cross")) {
                    waitForDistance(ditch) { acrossDitch() }
                }
            } else {
                pathToDitch.traverse(1, 5)
            }
        }
    }

    private fun acrossDitch() = me.tile().y >= 3523

    private val pathToDitch = listOf(Tile(3096, 3494, 0), Tile(3099, 3499, 0), Tile(3099, 3504, 0), Tile(3102, 3511, 0), Tile(3102, 3516, 0), Tile(3102, 3520, 0))
    private val toMage = listOf(Tile(3106, 3523, 0), Tile(3106, 3528, 0), Tile(3106, 3533, 0), Tile(3108, 3539, 0), Tile(3108, 3546, 0), Tile(3105, 3550, 0), Tile(3105, 3555, 0))
}