package org.powbot.krulvis.runecrafting.tree.leafs

import org.powbot.api.rt4.Prayer
import org.powbot.api.rt4.Skills
import org.powbot.api.rt4.walking.model.Skill
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.traverse
import org.powbot.krulvis.runecrafting.RuneAltar
import org.powbot.krulvis.runecrafting.Runecrafter

class MoveToAltar(script: Runecrafter) : Leaf<Runecrafter>(script, "Moving to Altar") {
    override fun execute() {
        val pray = script.prayer
        if (pray != null && Skills.realLevel(Skill.Prayer) >= pray.level() && Skills.level(Skill.Prayer) > 0) {
            Prayer.prayer(pray, true)
        }
        script.altar.pathToAltar.traverse(1, 3)
    }
}