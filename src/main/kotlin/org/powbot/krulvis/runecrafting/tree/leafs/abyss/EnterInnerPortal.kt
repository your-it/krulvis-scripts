package org.powbot.krulvis.runecrafting.tree.leafs.abyss

import org.powbot.api.rt4.Equipment
import org.powbot.api.rt4.GameObject
import org.powbot.api.rt4.Movement
import org.powbot.api.rt4.Objects
import org.powbot.api.script.tree.Leaf
import org.powbot.krulvis.api.ATContext.me
import org.powbot.krulvis.api.ATContext.walkAndInteract
import org.powbot.krulvis.api.extensions.Utils.waitForDistance
import org.powbot.krulvis.runecrafting.Runecrafter

class EnterInnerPortal(script: Runecrafter) : Leaf<Runecrafter>(script, "Entering Inner Portal") {
    override fun execute() {
        val pass = getPass()
        val neighbor = pass.tile
        val action = pass.actions().firstOrNull()
        script.logger.info("Found passthrough in abyss name=${pass.name}, action=${action}, neighbor=${neighbor}")
        if (!pass.inViewport() || pass.distance() > 8) {
            Movement.step(neighbor)
        } else if (walkAndInteract(pass, pass.actions()[0])) {
            waitForDistance(pass) { me.animation() != -1 }
        }
    }

    private val hasAxe by lazy { Equipment.stream().nameContains("axe").isNotEmpty() }
    private val hasPick by lazy { Equipment.stream().nameContains("pickaxe").isNotEmpty() }
    private val passNames by lazy {
        val l = mutableListOf("Gap", "Eyes", "Passage")
        if (hasPick) l.add("Rock")
        if (hasAxe) l.add("Tendrils")
        l.toTypedArray()
    }
    private val actions = listOf("Squeeze-through", "Distract", "Chop", "Go-through", "Mine")

    private fun getPass(): GameObject {
        return Objects.stream(50).type(GameObject.Type.INTERACTIVE).name(*passNames).action(actions).nearest().first()
    }
}