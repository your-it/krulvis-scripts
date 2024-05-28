package org.powbot.krulvis.fighter.tree.branch

import org.powbot.api.requirement.Requirement
import org.powbot.api.requirement.RunePowerRequirement
import org.powbot.api.rt4.Inventory
import org.powbot.api.rt4.Item
import org.powbot.api.rt4.Magic
import org.powbot.api.rt4.magic.RunePower
import org.powbot.api.script.tree.Branch
import org.powbot.api.script.tree.SimpleLeaf
import org.powbot.api.script.tree.TreeComponent
import org.powbot.krulvis.api.ATContext.getCount
import org.powbot.krulvis.api.utils.Utils
import org.powbot.krulvis.fighter.Fighter

class ShouldBuryBones(script: Fighter) : Branch<Fighter>(script, "Should Bury bones?") {

    var bones = emptyList<Item>()
    var ashes = emptyList<Item>()
    val actions = mapOf("bones" to "bury", "ashes" to "scatter")

    val offeringSpell = object : Magic.MagicSpell {
        override val requirements: Array<out Requirement> = arrayOf(
            RunePowerRequirement(RunePower.WRATH, 1),
            RunePowerRequirement(RunePower.SOUL, 1)
        )

        override fun book(): Magic.Book = Magic.Book.ARCEUUS

        override fun componentIndex(): Int = 174

        override fun level(): Int = 84

        override fun texture(): Int = -1
    }

    override val successComponent: TreeComponent<Fighter> = SimpleLeaf(script, "Bury bones") {
        if (offeringSpell.canCast()) {
            if (ashes.size >= 3 && offeringSpell.cast()) {
                Utils.waitFor { filterBones().size < bones.size }
            }
        } else {
            bones.forEachIndexed { i, bone ->
                val count = Inventory.getCount(bone.id)
                val action = bone.buryAction()
                script.logger.info("$action on ${bone.name()}")
                if (bone.interact(action)) {
                    Utils.waitFor { count > Inventory.getCount(bone.id) }
                    if (i < this.bones.size - 1)
                        Utils.sleep(1500)
                }
            }
        }
    }
    override val failedComponent: TreeComponent<Fighter> = ShouldBank(script)


    private fun Item.buryAction(): String = actions[actions.keys.first { name().contains(it, true) }] ?: "Bury"
    private fun filterBones() = Inventory.stream().filter { item ->
        val name = item.name().lowercase()
        actions.keys.any { name.contains(it, true) }
    }

    override fun validate(): Boolean {
        if (!script.buryBones) return false
        bones = filterBones()
        ashes = bones.filter { it.name().lowercase().contains("ashes") }
        return if (offeringSpell.canCast()) {
            ashes.size >= 3 || bones.any { it.name().lowercase().contains("bones") }
        } else {
            bones.isNotEmpty()
        }
    }
}