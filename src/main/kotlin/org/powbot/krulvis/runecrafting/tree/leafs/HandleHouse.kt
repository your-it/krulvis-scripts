package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.*
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter

class HandleHouse(script: Runecrafter) : Leaf<Runecrafter>(script, "Handling house") {

    override fun execute() {
        if (!useRestorePool()) return

        val glory = Objects.stream(50).type(GameObject.Type.WALL_DECORATION).name("Amulet of Glory").first()
        if (walkAndInteract(glory, "Edgeville")) {
            waitForDistance(glory) { script.getBank().valid() }
        }
    }

    private fun shouldRestorePool(): Boolean {
        return Skills.level(Skill.Hitpoints) < Skills.realLevel(Skill.Hitpoints) ||
                Skills.level(Skill.Prayer) < Skills.realLevel(Skill.Prayer) ||
                Combat.isPoisoned() ||
                Movement.energyLevel() < 90
    }

    private fun useRestorePool(): Boolean {
        if (!shouldRestorePool()) {
            return true
        }

        val pool = Objects.stream().nameContains("Pool of").first()

        if (!pool.valid()) {
            return true
        }

        return walkAndInteract(pool, "Drink") && waitForDistance(pool) { Movement.energyLevel() >= 99 }
    }


}